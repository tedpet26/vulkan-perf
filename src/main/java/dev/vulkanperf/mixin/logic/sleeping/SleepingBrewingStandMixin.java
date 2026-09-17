package dev.vulkanperf.mixin.logic.sleeping;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.vulkanperf.logic.sleeping.SleepingBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

/**
 * A brewing stand without fuel can never start brewing, and fuel insertion always calls
 * {@code setChanged}, which is the universal wake path. Stands holding fuel but nothing
 * brewable keep ticking (vanilla cost), since brewability depends on recipe lookups we would
 * have to redo anyway to decide sleep safety.
 */
@Mixin(BrewingStandBlockEntity.class)
public abstract class SleepingBrewingStandMixin {

	@Shadow
	private int brewTime;

	@Shadow
	private int fuel;

	@Inject(method = "serverTick", at = @At("RETURN"))
	private static void vulkanperf$maybeSleep(
		ServerLevel level, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state,
		BrewingStandBlockEntity entity, CallbackInfo ci
	) {
		SleepingBrewingStandMixin self = (SleepingBrewingStandMixin) (Object) entity;
		if (self.brewTime <= 0 && self.fuel <= 0) {
			((SleepingBlockEntity) entity).vp$startSleeping();
		}
	}

	@Inject(method = "loadAdditional", at = @At("RETURN"))
	private void vulkanperf$wakeOnLoad(net.minecraft.world.level.storage.ValueInput input, CallbackInfo ci) {
		((SleepingBlockEntity) this).vp$wakeUpNow();
	}
}
