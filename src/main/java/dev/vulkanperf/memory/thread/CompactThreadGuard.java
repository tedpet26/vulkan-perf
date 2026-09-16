package dev.vulkanperf.memory.thread;

import net.minecraft.util.ThreadingDetector;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * A single-byte-of-state stand-in for {@code ThreadingDetector}, intended for
 * very hot, very numerous objects (like {@code PalettedContainer}s) where the
 * full detector's semaphore + lock + volatile fields are wasteful. Concurrent
 * access is still detected and reported with vanilla's crash formatting; this
 * is purely a memory-shape change, not a behavior change, on the fast path.
 */
@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
public final class CompactThreadGuard {
	private CompactThreadGuard() {
	}

	public static void acquire(ThreadGuardState guard, String label) {
		byte previousState;
		synchronized (guard) {
			previousState = guard.vulkanperf$getGuardState();
			if (previousState == ThreadGuardState.UNLOCKED) {
				guard.vulkanperf$setGuardState(ThreadGuardState.LOCKED);
				return;
			} else if (previousState == ThreadGuardState.LOCKED) {
				CrashCoordinator.beginCrash(guard, label);
				guard.vulkanperf$setGuardState(ThreadGuardState.CRASHING);
			}
		}
		if (previousState == ThreadGuardState.LOCKED) {
			throw CrashCoordinator.waitForAcquireCrash(guard);
		} else {
			CrashCoordinator.waitAsBystander(guard);
		}
	}

	public static void release(ThreadGuardState guard) {
		byte previousState;
		synchronized (guard) {
			previousState = guard.vulkanperf$getGuardState();
			if (previousState == ThreadGuardState.LOCKED) {
				guard.vulkanperf$setGuardState(ThreadGuardState.UNLOCKED);
				return;
			}
		}
		if (previousState == ThreadGuardState.CRASHING) {
			throw CrashCoordinator.waitForReleaseCrash(guard);
		}
	}

	/**
	 * Holds the (rare) in-progress state needed to build a proper crash report
	 * naming both offending threads. None of this needs to be fast, since it
	 * only runs while a crash is already happening.
	 */
	private static final class CrashCoordinator {
		private static final Object LOCK = new Object();
		private static final Map<ThreadGuardState, PendingCrash> PENDING = new IdentityHashMap<>();

		static void beginCrash(ThreadGuardState guard, String label) {
			synchronized (LOCK) {
				PENDING.put(guard, new PendingCrash(label));
			}
		}

		static RuntimeException waitForAcquireCrash(ThreadGuardState guard) {
			return lookup(guard).waitForAcquirer();
		}

		static RuntimeException waitForReleaseCrash(ThreadGuardState guard) {
			return lookup(guard).waitForReleaser();
		}

		static void waitAsBystander(ThreadGuardState guard) {
			PendingCrash pending = lookup(guard);
			pending.waitUntilResolved();
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			throw new IllegalStateException("Bystander to a threading crash of " + pending.label);
		}

		private static PendingCrash lookup(ThreadGuardState guard) {
			synchronized (LOCK) {
				return Objects.requireNonNull(PENDING.get(guard));
			}
		}

		private static final class PendingCrash {
			private final String label;
			private Thread acquirer;
			private Thread releaser;
			private RuntimeException resolved;

			private PendingCrash(String label) {
				this.label = label;
			}

			synchronized RuntimeException waitForAcquirer() {
				this.acquirer = Thread.currentThread();
				notifyAll();
				waitUntil(() -> this.releaser != null);
				this.resolved = ThreadingDetector.makeThreadingException(this.label, this.releaser);
				notifyAll();
				return this.resolved;
			}

			synchronized RuntimeException waitForReleaser() {
				this.releaser = Thread.currentThread();
				notifyAll();
				waitUntil(() -> this.resolved != null);
				return this.resolved;
			}

			synchronized void waitUntilResolved() {
				waitUntil(() -> this.resolved != null);
			}

			private synchronized void waitUntil(BooleanSupplier condition) {
				long deadline = System.currentTimeMillis() + 60_000L;
				while (!condition.getAsBoolean()) {
					long remaining = deadline - System.currentTimeMillis();
					if (remaining <= 0) {
						throw new IllegalStateException("Threading crash coordination timed out waiting for " + this.label);
					}
					try {
						wait(Math.min(remaining, 10_000L));
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						return;
					}
				}
			}
		}
	}
}
