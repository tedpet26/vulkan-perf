package dev.vulkanperf.logic.sleeping;

/**
 * Duck API added to {@code BlockEntity}: idle block entities can park their ticker (the chunk's
 * rebindable wrapper is pointed at a no-op) until one of their inputs changes. Any change that
 * flows through {@code BlockEntity#setChanged}, a ticker rebind, or a registered wake source
 * restores the real ticker.
 */
public interface SleepingBlockEntity {
	/** Installed by the chunk ticker hook whenever the wrapper identity is (re)established. */
	void vp$setTickWrapper(TickerWrapper wrapper);

	/** A fresh real ticker was installed (state change, reload); forget any parked state. */
	void vp$clearSleeping();

	void vp$startSleeping();

	void vp$wakeUpNow();

	/** Sleep variant that wakes exactly on the next tick (vanilla-parity cooldown timing). */
	void vp$sleepOnlyCurrentTick();

	boolean vp$isSleeping();

	/** Duck over {@code LevelChunk.RebindableTickingBlockEntityWrapper}. */
	interface TickerWrapper {
		net.minecraft.world.level.block.entity.TickingBlockEntity vp$getWrapped();

		void vp$rebindWrapped(net.minecraft.world.level.block.entity.TickingBlockEntity ticker);
	}
}
