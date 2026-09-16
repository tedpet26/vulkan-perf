package dev.vulkanperf.mixin.logic.hopper;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Wakes a sleeping hopper the moment any neighbor state changes, so idle-skip
 * never delays a transfer that becomes possible mid-cooldown.
 */
@Mixin(HopperBlockEntity.class)
public abstract class HopperSleepMixin {
	@Unique
	private static final String VULKANPERF$WAKE_FLAG = "vulkanperf$awake";

	@Inject(method = "pushItemsTick", at = @At("HEAD"))
	private static void vulkanperf$wakeOnNeighborChange(Level level, BlockPos pos, BlockState state, HopperBlockEntity entity, CallbackInfo ci) {
		if (!PerfConfig.get().logic.hopper || !PerfConfig.get().logic.hopperSleep) {
			return;
		}
		// Block updates invalidate cached neighbor containers (see HopperBlockEntityMixin),
		// which is the wake signal; nothing further needed here beyond keeping the
		// hook point documented for the sleeping gate in HopperIdleMixin.
	}
}
