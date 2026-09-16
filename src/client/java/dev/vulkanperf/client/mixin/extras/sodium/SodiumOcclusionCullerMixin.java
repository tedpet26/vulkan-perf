package dev.vulkanperf.client.mixin.extras.sodium;

import dev.vulkanperf.client.extras.ExtrasRuntime;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OcclusionCuller.class)
public abstract class SodiumOcclusionCullerMixin {
	@Inject(method = "testDistance", at = @At("HEAD"), cancellable = true)
	private static void vulkanperf$disableFogDistance(float horizontalDistanceSquared, float verticalDistance, float distanceLimit, CallbackInfoReturnable<Boolean> cir) {
		if (!ExtrasRuntime.fog()) {
			cir.setReturnValue(true);
		}
	}
}
