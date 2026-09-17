package dev.vulkanperf.mixin.logic.sleeping;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.vulkanperf.logic.sleeping.SleepingBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

/**
 * A fully closed shulker box's animation ticker just re-asserts zero progress every tick.
 * {@code triggerEvent} (open/close block events, both sides) is the wake path, plus load.
 */
@Mixin(ShulkerBoxBlockEntity.class)
public abstract class SleepingShulkerBoxMixin {

	@Shadow
	private float progress;

	@Shadow
	public abstract ShulkerBoxBlockEntity.AnimationStatus getAnimationStatus();

	@Inject(method = "updateAnimation", at = @At("RETURN"))
	private void vulkanperf$sleepWhenClosed(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state, CallbackInfo ci) {
		if (this.progress == 0.0F && this.getAnimationStatus() == ShulkerBoxBlockEntity.AnimationStatus.CLOSED) {
			((SleepingBlockEntity) (Object) this).vp$startSleeping();
		}
	}

	@Inject(method = "triggerEvent", at = @At("HEAD"))
	private void vulkanperf$wakeOnBlockEvent(int id, int param, CallbackInfoReturnable<Boolean> cir) {
		((SleepingBlockEntity) (Object) this).vp$wakeUpNow();
	}

	@Inject(method = "loadAdditional", at = @At("RETURN"))
	private void vulkanperf$wakeOnLoad(net.minecraft.world.level.storage.ValueInput input, CallbackInfo ci) {
		((SleepingBlockEntity) (Object) this).vp$wakeUpNow();
	}
}
