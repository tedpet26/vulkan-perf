package dev.vulkanperf.client.mixin.imfast;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import dev.vulkanperf.client.imfast.ImFastRuntime;
import dev.vulkanperf.client.imfast.map.MapAtlasAllocator;
import dev.vulkanperf.client.imfast.map.MapRenderStateExtension;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.MapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMapMixin {
	@WrapOperation(
		method = "map",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;innerBlit(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Lcom/mojang/renderpearl/api/textures/GpuSampler;IIIIFFFFI)V",
			ordinal = 0
		)
	)
	private void vulkanperf$atlasMapBlit(
		GuiGraphicsExtractor instance,
		RenderPipeline pipeline,
		GpuTextureView textureView,
		GpuSampler sampler,
		int x0,
		int y0,
		int x1,
		int y1,
		float u0,
		float u1,
		float v0,
		float v1,
		int color,
		Operation<Void> original,
		@Local(argsOnly = true) MapRenderState mapRenderState
	) {
		MapRenderStateExtension ext = (MapRenderStateExtension) mapRenderState;
		if (ext.vulkanperf$getAtlasTextureId() != null && ext.vulkanperf$getAtlasTextureId().equals(mapRenderState.texture)) {
			int atlasSize = ImFastRuntime.mapAtlasSize();
			u0 = (float) ext.vulkanperf$getAtlasX() / atlasSize;
			u1 = (float) (ext.vulkanperf$getAtlasX() + MapAtlasAllocator.MAP_SIZE) / atlasSize;
			v0 = (float) ext.vulkanperf$getAtlasY() / atlasSize;
			v1 = (float) (ext.vulkanperf$getAtlasY() + MapAtlasAllocator.MAP_SIZE) / atlasSize;
		}
		original.call(instance, pipeline, textureView, sampler, x0, y0, x1, y1, u0, u1, v0, v1, color);
	}
}
