package dev.vulkanperf.client.mixin.clientcache;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public abstract class ToastManagerMixin {
	@Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$skipHidden(Toast toast, CallbackInfo ci) {
		if (!PerfConfig.get().clientcache.toasts) {
			return;
		}
		var client = net.minecraft.client.Minecraft.getInstance();
		if (client != null && !client.isWindowActive()) {
			ci.cancel();
		}
	}
}
