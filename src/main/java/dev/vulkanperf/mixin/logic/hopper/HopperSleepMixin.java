package dev.vulkanperf.mixin.logic.hopper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.vulkanperf.config.PerfConfig;import dev.vulkanperf.logic.hopper.ContainerChangeBus;
import dev.vulkanperf.logic.sleeping.SleepingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Puts quiescent hoppers to sleep and wakes them on exactly the inputs that could change a
 * transfer outcome: own/source/target container changes (via {@link ContainerChangeBus}),
 * block updates around the hopper, and entities entering the item-pickup zone.
 *
 * <p>Cooldown parity: a hopper that goes to sleep mid-cooldown compensates the slept ticks at
 * its next real tick, so transfer timing matches vanilla countdown behaviour.
 */
@Mixin(HopperBlockEntity.class)
public abstract class HopperSleepMixin {
	@Shadow
	private int cooldownTime;

	@Shadow
	protected abstract boolean isOnCustomCooldown();

	@Unique
	private boolean vulkanperf$wasSleeping;

	@Unique
	private long vulkanperf$sleepGameTime;

	@Inject(method = "pushItemsTick", at = @At("HEAD"))
	private static void vulkanperf$compensateSleptCooldown(
		Level level, BlockPos pos, BlockState state, HopperBlockEntity entity, CallbackInfo ci
	) {
		HopperSleepMixin self = (HopperSleepMixin) (Object) entity;
		if (self.vulkanperf$wasSleeping) {
			self.vulkanperf$wasSleeping = false;
			if (self.cooldownTime > 0) {
				long slept = level.getGameTime() - self.vulkanperf$sleepGameTime;
				if (slept > 0) {
					self.cooldownTime = (int) Math.max(0, self.cooldownTime - slept);
				}
			}
		}
	}

	@Inject(method = "pushItemsTick", at = @At("RETURN"))
	private static void vulkanperf$maybeSleep(Level level, BlockPos pos, BlockState state, HopperBlockEntity entity, CallbackInfo ci) {
		PerfConfig.LogicConfig logic = PerfConfig.get().logic;
		if (!logic.hopper || !logic.hopperSleep || !logic.sleepingBlockEntities) {
			return;
		}
		if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
			return;
		}
		HopperSleepMixin self = (HopperSleepMixin) (Object) entity;
		SleepingBlockEntity sleeper = (SleepingBlockEntity) entity;
		if (sleeper.vp$isSleeping()) {
			return;
		}
		if (self.isOnCustomCooldown()) {
			return;
		}
		BlockPos above = pos.above();
		BlockPos front = pos.relative(state.getValue(HopperBlock.FACING));
		if (!vulkanperf$canWatch(serverLevel, pos, above) || !vulkanperf$canWatch(serverLevel, pos, front)) {
			return;
		}
		AABB suckZone = new AABB(pos.getX(), pos.getY() + 1.0, pos.getZ(), pos.getX() + 1.0, pos.getY() + 2.0, pos.getZ() + 1.0);
		if (!level.getEntitiesOfClass(ItemEntity.class, suckZone).isEmpty()) {
			return;
		}
		ContainerChangeBus.watch(serverLevel, pos, pos);
		ContainerChangeBus.watch(serverLevel, pos, above);
		ContainerChangeBus.watch(serverLevel, pos, front);
		self.vulkanperf$sleepGameTime = level.getGameTime();
		self.vulkanperf$wasSleeping = true;
		sleeper.vp$startSleeping();
	}

	/**
	 * Hoppers may only sleep when every container that affects them is a block entity whose
	 * changes flow through {@code setChanged}. Entity containers (minecarts, chest boats) are
	 * not watchable, so a hopper reading from or pushing into one stays awake.
	 */
	private static boolean vulkanperf$canWatch(net.minecraft.server.level.ServerLevel level, BlockPos sleeperPos, BlockPos target) {
		net.minecraft.world.Container container = HopperBlockEntity.getContainerAt(level, target);
		if (container == null) {
			return true;
		}
		return container instanceof BlockEntity;
	}

	/**
	 * Wake parity: 26.3's hopper inventory writes flow through {@code setItem} without calling
	 * {@code setChanged}, so both this hopper's own sleep state and the watcher bus need the
	 * explicit signal.
	 */
	@Inject(method = "setItem", at = @At("RETURN"))
	private void vulkanperf$wakeOnItemWrite(int slot, net.minecraft.world.item.ItemStack stack, CallbackInfo cir) {
		((SleepingBlockEntity) this).vp$wakeUpNow();
		if (PerfConfig.get().logic.hopper) {
			ContainerChangeBus.onBlockEntityChanged((BlockEntity) (Object) this);
		}
	}
}
