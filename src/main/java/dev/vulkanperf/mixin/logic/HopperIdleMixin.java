package dev.vulkanperf.mixin.logic;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
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
 * Extends the transfer cooldown while a hopper stays empty, cutting empty-pipe
 * work. With {@code logic.hopperSleep}, any {@code setItem} on the hopper's
 * own inventory (item added by a player or another hopper) resets the idle
 * escalation immediately, so idle-skipping never delays a transfer that just
 * became possible.
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

	/**
	 * Wake signal: the hopper's inventory changed, so the empty-idle assumption
	 * no longer holds. Reset the escalation so the vanilla cooldown applies.
	 *
	 * <p>26.3 note: {@code HopperBlockEntity} no longer overrides
	 * {@code setChanged}; {@code setItem} is the inventory-mutation hook, so
	 * the wake now rides on it instead.
	 */
	@Inject(method = "setItem", at = @At("RETURN"))
	private void vulkanperf$wakeOnActivity(int slot, ItemStack stack, CallbackInfo ci) {
		PerfConfig.LogicConfig logic = PerfConfig.get().logic;
		if (logic.enabled && logic.hopperSleep && this.vulkanperf$idleStreak != 0) {
			this.vulkanperf$idleStreak = 0;
		}
	}
}
