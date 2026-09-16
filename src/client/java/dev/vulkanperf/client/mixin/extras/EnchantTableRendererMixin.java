package dev.vulkanperf.client.mixin.extras;

import dev.vulkanperf.client.extras.ExtrasRuntime;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantTableRenderer.class)
public abstract class EnchantTableRendererMixin {
	@Inject(method = "submit", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$book(CallbackInfo ci) {
		if (!ExtrasRuntime.enchantingTableBook()) {
			ci.cancel();
		}
	}
}
