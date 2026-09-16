package dev.vulkanperf.client.mixin.extras;

import dev.vulkanperf.client.extras.ExtrasRuntime;
import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonHeadRenderer.class)
public abstract class PistonHeadRendererMixin {
	@Inject(method = "submit", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$pistons(CallbackInfo ci) {
		if (!ExtrasRuntime.pistons()) {
			ci.cancel();
		}
	}
}
