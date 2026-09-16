package dev.vulkanperf.memory.thread;

/**
 * Duck interface implemented by the {@code PalettedContainer} mixin to store
 * a single byte of guard state instead of a full {@code ThreadingDetector}
 * object (semaphore + lock + volatile fields).
 */
public interface ThreadGuardState {
	byte UNLOCKED = 0;
	byte LOCKED = 1;
	byte CRASHING = 2;

	byte vulkanperf$getGuardState();

	void vulkanperf$setGuardState(byte state);
}
