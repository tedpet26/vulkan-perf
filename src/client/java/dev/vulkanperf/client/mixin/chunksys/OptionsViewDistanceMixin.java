package dev.vulkanperf.client.mixin.chunksys;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Raises the client render-distance slider cap beyond vanilla's 32 when
 * enabled. Purely client-side; the server still governs what is sent.
 */
@Mixin(Options.class)
public abstract class OptionsViewDistanceMixin {
	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;noTooltip(Lnet/minecraft/client/OptionInstance$IntRange;)Lnet/minecraft/client/OptionInstance$TooltipSupplier;"), require = 0)
	private static int vulkanperf$uncapViewDistance(int max) {
		PerfConfig.ChunksConfig config = PerfConfig.get().chunks;
		if (config.enabled && config.clientViewDistanceUncap && config.clientMaxViewDistance > max) {
			return config.clientMaxViewDistance;
		}
		return max;
	}
}
