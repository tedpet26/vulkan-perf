package dev.vulkanperf.client.mixin.imfast;

import com.mojang.renderpearl.backend.opengl.GlDebug;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GlDebug.class)
public abstract class GlDebugInfoMixin {
	@Unique
	private static long vulkanperf$lastLogMs;

	@ModifyVariable(method = "enableDebugCallback", at = @At("HEAD"), name = "debugSynchronousGlLogs", argsOnly = true)
	private static boolean vulkanperf$syncGlDebug(boolean debugSynchronousGlLogs) {
		return true;
	}

	@Redirect(method = "printDebugLog", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V"))
	private void vulkanperf$glDebugStack(Logger logger, String message, Object argument) {
		long now = System.currentTimeMillis();
		if (now - vulkanperf$lastLogMs > 1000L) {
			vulkanperf$lastLogMs = now;
			logger.info(message, argument, new Exception("OpenGL debug"));
		} else {
			logger.info(message, argument);
		}
	}
}
