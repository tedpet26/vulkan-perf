package dev.vulkanperf.mixin.logic;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * FluidState#isRandomlyTicking() re-dispatches to the owning fluid on every call, and the
 * random-tick loop asks the same handful of states (water, lava) thousands of times per tick.
 * The answer is constant per state instance, so cache it on first query.
 */
@Mixin(FluidState.class)
public abstract class FluidStateRandomTickMixin {
	@Unique
	private boolean vulkanperf$randomTickKnown;
	@Unique
	private boolean vulkanperf$randomTickValue;

	@Inject(method = "isRandomlyTicking", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$cachedRandomTick(CallbackInfoReturnable<Boolean> cir) {
		if (!PerfConfig.get().logic.fluidRandomTickCache) {
			return;
		}
		if (this.vulkanperf$randomTickKnown) {
			cir.setReturnValue(this.vulkanperf$randomTickValue);
		}
	}

	@Inject(method = "isRandomlyTicking", at = @At("RETURN"))
	private void vulkanperf$storeRandomTick(CallbackInfoReturnable<Boolean> cir) {
		this.vulkanperf$randomTickValue = cir.getReturnValueZ();
		this.vulkanperf$randomTickKnown = true;
	}
}
