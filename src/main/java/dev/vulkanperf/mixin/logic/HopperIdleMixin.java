package dev.vulkanperf.mixin.logic;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Extends transfer cooldown when a hopper stays empty, cutting empty-pipe work.
 */
@Mixin(HopperBlockEntity.class)
public abstract class HopperIdleMixin {
	@Shadow
	private int cooldownTime;

	@Unique
	private int vulkanperf$idleStreak;

	@Inject(method = "pushItemsTick", at = @At("RETURN"))
	private static void vulkanperf$extendOnIdle(Level level, BlockPos pos, BlockState state, HopperBlockEntity entity, CallbackInfo ci) {
		if (!PerfConfig.get().logic.hopper) {
			return;
		}
		HopperIdleMixin self = (HopperIdleMixin) (Object) entity;
		Container container = entity;
		if (container.isEmpty()) {
			self.vulkanperf$idleStreak = Math.min(self.vulkanperf$idleStreak + 1, 8);
			if (self.cooldownTime <= 0) {
				self.cooldownTime = 8 + self.vulkanperf$idleStreak * 4;
			}
		} else {
			self.vulkanperf$idleStreak = 0;
		}
	}
}
