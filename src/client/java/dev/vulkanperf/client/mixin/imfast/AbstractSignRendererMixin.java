package dev.vulkanperf.client.mixin.imfast;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import dev.vulkanperf.VulkanPerf;
import dev.vulkanperf.client.imfast.ImFastRuntime;
import dev.vulkanperf.client.imfast.sign.SignTextAtlas;
import dev.vulkanperf.client.imfast.sign.SignTextCache;
import dev.vulkanperf.client.imfast.sign.SignTextExtension;
import dev.vulkanperf.client.mixin.imfast.accessors.GameRendererImFastAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.entity.SignText;
import org.joml.Matrix4fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

@Mixin(AbstractSignRenderer.class)
public abstract class AbstractSignRendererMixin {
	@Shadow
	@Final
	private Font font;

	@Shadow
	private void submitSignText(SignRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, SignText signText) {
		throw new AssertionError();
	}

	@Inject(method = "submitSignText", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$bufferedSignText(SignRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, SignText signText, CallbackInfo ci) {
		if (!ImFastRuntime.signTextBuffering() || !(signText instanceof SignTextExtension ext) || !ext.vulkanperf$shouldCache()) {
			return;
		}
		SignTextCache cache = ImFastRuntime.signTextCache();
		if (cache == null) {
			return;
		}
		SignTextAtlas.Slot slot = cache.slots.getIfPresent(signText);
		if (slot == null) {
			int width = this.vulkanperf$signWidth(signText, state.isTextFilteringEnabled, state.maxTextLineWidth);
			int height = 4 * state.textLineHeight;
			if (width <= 0 || height <= 0) {
				ext.vulkanperf$setShouldCache(false);
				return;
			}
			int padding = signText.hasGlowingText() ? 2 : 0;
			slot = cache.atlas.findSlot(width + padding, height + padding);
			if (slot == null) {
				VulkanPerf.LOGGER.warn("Sign text atlas is full ({} entries); falling back to immediate text", cache.slots.size());
				ext.vulkanperf$setShouldCache(false);
				return;
			}
			if (!this.vulkanperf$rasterizeSign(state, signText, ext, cache, slot)) {
				ext.vulkanperf$setShouldCache(false);
				return;
			}
			cache.slots.put(signText, slot);
		}

		int atlas = SignTextAtlas.atlasSize();
		float u0 = (float) slot.x / atlas;
		float u1 = (float) (slot.x + slot.width) / atlas;
		float v0 = 1.0F - ((float) slot.y / atlas);
		float v1 = 1.0F - ((float) (slot.y + slot.height) / atlas);
		int light = signText.hasGlowingText() ? LightCoordsUtil.FULL_BRIGHT : state.lightCoords;
		poseStack.pushPose();
		poseStack.translate(-slot.width / 2.0F, -slot.height / 2.0F, 0.0F);
		SignTextAtlas.Slot blitSlot = slot;
		submitNodeCollector.submitCustomGeometry(poseStack, cache.renderType, (pose, buffer) -> {
			buffer.addVertex(pose, 0.0F, blitSlot.height, 0.0F).setColor(255, 255, 255, 255).setUv(u0, v1).setLight(light);
			buffer.addVertex(pose, blitSlot.width, blitSlot.height, 0.0F).setColor(255, 255, 255, 255).setUv(u1, v1).setLight(light);
			buffer.addVertex(pose, blitSlot.width, 0.0F, 0.0F).setColor(255, 255, 255, 255).setUv(u1, v0).setLight(light);
			buffer.addVertex(pose, 0.0F, 0.0F, 0.0F).setColor(255, 255, 255, 255).setUv(u0, v0).setLight(light);
		});
		poseStack.popPose();
		ci.cancel();
	}

	@Unique
	private boolean vulkanperf$rasterizeSign(
		SignRenderState state,
		SignText signText,
		SignTextExtension ext,
		SignTextCache cache,
		SignTextAtlas.Slot slot
	) {
		Minecraft client = Minecraft.getInstance();
		RenderSystem.backupProjectionMatrix();
		RenderSystem.setProjectionMatrix(cache.projection, ProjectionType.ORTHOGRAPHIC);
		Matrix4fStack modelView = RenderSystem.getModelViewStack();
		modelView.pushMatrix().identity();
		GpuBufferSlice previousFog = RenderSystem.getShaderFog();
		FogRenderer fogRenderer = ((GameRendererImFastAccessor) client.gameRenderer).vulkanperf$fogRenderer();
		RenderSystem.setShaderFog(fogRenderer.getBuffer(FogRenderer.FogMode.NONE));
		ext.vulkanperf$setShouldCache(false);
		boolean ok = false;
		try {
			PoseStack textPose = new PoseStack();
			textPose.translate(slot.x, slot.y, 0.0F);
			textPose.translate(slot.width / 2.0F, slot.height / 2.0F, 0.0F);
			state.drawOutline = true;
			SubmitNodeStorage storage = new SubmitNodeStorage();
			this.submitSignText(state, textPose, storage, signText);
			FeatureRenderDispatcher dispatcher = client.gameRenderer.featureRenderDispatcher();
			int atlas = SignTextAtlas.atlasSize();
			RenderSystem.enableScissorForRenderTypeDraws(slot.x, atlas - slot.y - slot.height, slot.width, slot.height);
			try (
				FeatureRenderDispatcher.PreparedFrame frame = dispatcher.prepareFrame(storage);
				RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
					() -> "vulkanperf sign atlas",
					cache.atlas.getColorTextureView(),
					Optional.empty(),
					cache.atlas.getDepthTextureView(),
					OptionalDouble.empty()
				)
			) {
				RenderSystem.bindDefaultUniforms(pass);
				FeatureRenderDispatcher.renderAllFeatures(pass, frame);
			}
			RenderSystem.disableScissorForRenderTypeDraws();
			ok = true;
		} catch (RuntimeException e) {
			VulkanPerf.LOGGER.warn("Failed to rasterize sign text; using immediate mode", e);
		} finally {
			ext.vulkanperf$setShouldCache(true);
			if (previousFog != null) {
				RenderSystem.setShaderFog(previousFog);
			}
			modelView.popMatrix();
			RenderSystem.restoreProjectionMatrix();
		}
		return ok;
	}

	@Unique
	private int vulkanperf$signWidth(SignText signText, boolean filterText, int maxLineWidth) {
		FormattedCharSequence[] lines = signText.getRenderMessages(filterText, text -> {
			List<FormattedCharSequence> split = this.font.split(text, maxLineWidth);
			return split.isEmpty() ? FormattedCharSequence.EMPTY : split.get(0);
		});
		int width = 0;
		for (FormattedCharSequence line : lines) {
			width = Math.max(width, this.font.width(line));
		}
		if ((width & 1) != 0) {
			width++;
		}
		return width;
	}
}
