package dev.vulkanperf.mixin.logic.sleeping;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.vulkanperf.config.PerfConfig;
import dev.vulkanperf.logic.hopper.ContainerChangeBus;
import dev.vulkanperf.logic.sleeping.SleepingBlockEntity;
import dev.vulkanperf.logic.sleeping.SleepingTickers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;

/**
 * The sleeping block entity state machine. A sleeping block entity's wrapper points at a no-op
 * ticker; any {@code setChanged} (vanilla or modded, any slot mutation, command edit) restores
 * the real ticker, which makes this safe as a universal wake path.
 */
@Mixin(BlockEntity.class)
public abstract class BlockEntitySleepMixin implements SleepingBlockEntity {
	@Unique
	private TickerWrapper vp$wrapper;

	@Unique
	private TickingBlockEntity vp$realTicker;

	@Unique
	private boolean vp$sleeping;

	@Override
	public void vp$setTickWrapper(TickerWrapper wrapper) {
		this.vp$wrapper = wrapper;
		this.vp$clearSleeping();
	}

	@Override
	public void vp$clearSleeping() {
		this.vp$sleeping = false;
		this.vp$realTicker = null;
	}

	@Override
	public void vp$startSleeping() {
		if (this.vp$sleeping || this.vp$wrapper == null) {
			return;
		}
		TickingBlockEntity current = this.vp$wrapper.vp$getWrapped();
		if (current == null) {
			return;
		}
		this.vp$realTicker = current;
		this.vp$wrapper.vp$rebindWrapped(SleepingTickers.idle((BlockEntity) (Object) this, current));
		this.vp$sleeping = true;
	}

	@Override
	public void vp$wakeUpNow() {
		if (!this.vp$sleeping) {
			return;
		}
		this.vp$sleeping = false;
		if (this.vp$realTicker != null && this.vp$wrapper != null) {
			this.vp$wrapper.vp$rebindWrapped(this.vp$realTicker);
		}
		this.vp$realTicker = null;
	}

	@Override
	public void vp$sleepOnlyCurrentTick() {
		if (!this.vp$sleeping || this.vp$wrapper == null) {
			return;
		}
		TickingBlockEntity real = this.vp$realTicker;
		this.vp$sleeping = false;
		this.vp$realTicker = null;
		this.vp$wrapper.vp$rebindWrapped(SleepingTickers.wakeNextTick(this, (BlockEntity) (Object) this, real));
	}

	@Override
	public boolean vp$isSleeping() {
		return this.vp$sleeping;
	}

	@Inject(method = "setChanged", at = @At("HEAD"))
	private void vp$wakeOnSetChanged(CallbackInfo ci) {
		if (this.vp$sleeping) {
			this.vp$wakeUpNow();
		}
		if (PerfConfig.get().logic.hopper) {
			ContainerChangeBus.onBlockEntityChanged((BlockEntity) (Object) this);
		}
	}
}
