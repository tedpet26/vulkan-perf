package dev.vulkanperf.client.mixin.imfast;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.renderpearl.backend.opengl.GlConst;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import dev.vulkanperf.client.imfast.ImFastRuntime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlCommandEncoder")
public abstract class GlCommandEncoderFramebufferMixin {
	@WrapWithCondition(method = "submitRenderPass", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/GlStateManager;_glBindFramebuffer(II)V"))
	private boolean vulkanperf$skipUselessUnbind(int target, int framebuffer) {
		return !ImFastRuntime.avoidRedundantFramebufferSwitching();
	}

	@Inject(method = "presentTexture", at = @At("HEAD"))
	private void vulkanperf$unbindBeforePresent(CallbackInfo ci) {
		if (ImFastRuntime.avoidRedundantFramebufferSwitching()) {
			GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0);
		}
	}
}
