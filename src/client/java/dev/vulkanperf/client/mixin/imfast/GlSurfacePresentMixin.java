package dev.vulkanperf.client.mixin.imfast;

import com.mojang.renderpearl.backend.opengl.GlConst;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.backend.opengl.GlSurface;
import dev.vulkanperf.client.imfast.ImFastRuntime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlSurface.class)
public abstract class GlSurfacePresentMixin {
	@Inject(method = "present", at = @At("HEAD"))
	private void vulkanperf$unbindBeforeSwap(CallbackInfo ci) {
		if (ImFastRuntime.avoidRedundantFramebufferSwitching()) {
			GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0);
		}
	}
}
