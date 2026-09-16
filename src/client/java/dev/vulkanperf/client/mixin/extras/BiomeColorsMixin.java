package dev.vulkanperf.client.mixin.extras;

import dev.vulkanperf.client.extras.ExtrasRuntime;
import net.minecraft.client.renderer.BiomeColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeColors.class)
public abstract class BiomeColorsMixin {
	@Inject(method = "getAverageGrassColor", at = @At("RETURN"), cancellable = true)
	private static void vulkanperf$grass(CallbackInfoReturnable<Integer> cir) {
		if (!ExtrasRuntime.biomeColors()) {
			cir.setReturnValue(9551193);
		}
	}

	@Inject(method = "getAverageWaterColor", at = @At("RETURN"), cancellable = true)
	private static void vulkanperf$water(CallbackInfoReturnable<Integer> cir) {
		if (!ExtrasRuntime.biomeColors()) {
			cir.setReturnValue(4159204);
		}
	}

	@Inject(method = "getAverageFoliageColor", at = @At("RETURN"), cancellable = true)
	private static void vulkanperf$foliage(CallbackInfoReturnable<Integer> cir) {
		if (!ExtrasRuntime.biomeColors()) {
			cir.setReturnValue(5877296);
		}
	}
}
