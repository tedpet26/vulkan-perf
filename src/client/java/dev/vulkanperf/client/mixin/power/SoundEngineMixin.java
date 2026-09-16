package dev.vulkanperf.client.mixin.power;

import dev.vulkanperf.client.power.PowerController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
	@Inject(method = "play", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$mutePlay(SoundInstance instance, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
		if (PowerController.shouldMute(Minecraft.getInstance())) {
			cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
		}
	}

	@Inject(method = "playDelayed", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$muteDelayed(SoundInstance instance, int delay, CallbackInfo ci) {
		if (PowerController.shouldMute(Minecraft.getInstance())) {
			ci.cancel();
		}
	}
}
