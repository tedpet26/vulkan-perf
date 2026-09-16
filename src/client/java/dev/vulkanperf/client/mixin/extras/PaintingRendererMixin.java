package dev.vulkanperf.client.mixin.extras;

import dev.vulkanperf.client.extras.ExtrasRuntime;
import net.minecraft.client.renderer.entity.PaintingRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PaintingRenderer.class)
public abstract class PaintingRendererMixin {
	@Inject(method = "submit", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$paintings(CallbackInfo ci) {
		if (!ExtrasRuntime.paintings()) {
			ci.cancel();
		}
	}
}
