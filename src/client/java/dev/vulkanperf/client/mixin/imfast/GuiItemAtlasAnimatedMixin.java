package dev.vulkanperf.client.mixin.imfast;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.GpuTexture;
import dev.vulkanperf.client.imfast.gui.AnimatedItemAtlas;
import dev.vulkanperf.client.mixin.imfast.accessors.DynamicAtlasAllocatorAccessor;
import dev.vulkanperf.client.mixin.imfast.accessors.DynamicAtlasSlotAccessor;
import dev.vulkanperf.client.mixin.imfast.accessors.GuiItemAtlasAccessor;
import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiItemAtlas.class)
public abstract class GuiItemAtlasAnimatedMixin {
	@Inject(method = "endFrame", at = @At("HEAD"))
	private void vulkanperf$clearAnimatedAtlas(CallbackInfo ci) {
		if (!((Object) this instanceof AnimatedItemAtlas animated)) {
			return;
		}
		GuiItemAtlasAccessor atlasAccess = (GuiItemAtlasAccessor) (Object) this;
		DynamicAtlasAllocatorAccessor allocatorAccess = (DynamicAtlasAllocatorAccessor) atlasAccess.vulkanperf$allocator();
		int used = allocatorAccess.vulkanperf$usedSlotByKey().size();
		animated.recordUsedSlots(used);
		if (used > 0) {
			GpuTexture color = atlasAccess.vulkanperf$texture();
			GpuTexture depth = atlasAccess.vulkanperf$depthTexture();
			RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(color, GuiRenderer.CLEAR_COLOR, depth, 0.0);
			for (Object slot : allocatorAccess.vulkanperf$usedSlotByKey().values()) {
				((DynamicAtlasSlotAccessor) slot).vulkanperf$setFresh(true);
			}
		}
	}
}
