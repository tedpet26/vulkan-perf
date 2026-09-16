package dev.vulkanperf.client.mixin.extras;

import dev.vulkanperf.client.extras.ExtrasRuntime;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelLightEngine.class)
public abstract class LevelLightEngineMixin {
	@Inject(method = "runLightUpdates", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$lightUpdates(CallbackInfoReturnable<Integer> cir) {
		if (!ExtrasRuntime.lightUpdates()) {
			cir.setReturnValue(0);
		}
	}
}
