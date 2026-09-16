package dev.vulkanperf.client.mixin.extras;

import dev.vulkanperf.client.extras.ExtrasRuntime;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public abstract class ToastFilterMixin {
	@Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$toasts(Toast toast, CallbackInfo ci) {
		if (!ExtrasRuntime.toasts()) {
			ci.cancel();
		}
	}
}
