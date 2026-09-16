package dev.vulkanperf.chunks;

import dev.vulkanperf.VulkanPerf;
import dev.vulkanperf.config.PerfConfig;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public final class ChunkWorkers {
	private static ExecutorService worldgen;
	private static ExecutorService serialize;
	private static ExecutorService io;
	private static final AtomicBoolean fallback = new AtomicBoolean();
	private static final ConcurrentHashMap<Long, Object> writeLocks = new ConcurrentHashMap<>();
	private static final ThreadLocal<int[]> noiseScratch = ThreadLocal.withInitial(() -> new int[16]);

	private ChunkWorkers() {
	}

	public static synchronized void start() {
		if (worldgen != null) {
			return;
		}
		int gen = Math.max(1, PerfConfig.get().chunks.worldgenThreads);
		int serial = Math.max(1, PerfConfig.get().chunks.serializeThreads);
		int disk = Math.max(1, PerfConfig.get().chunks.ioThreads);
		worldgen = Executors.newFixedThreadPool(gen, named("vulkanperf-worldgen"));
		serialize = Executors.newFixedThreadPool(serial, named("vulkanperf-serialize"));
		io = Executors.newFixedThreadPool(disk, named("vulkanperf-chunkio"));
		VulkanPerf.LOGGER.info("chunk workers: worldgen={}, serialize={}, io={}", gen, serial, disk);
	}

	public static Executor worldgen(Executor vanilla) {
		return wrap(vanilla, worldgen());
	}

	public static Executor serialize(Executor vanilla) {
		return wrap(vanilla, serialize());
	}

	public static Executor io(Executor vanilla) {
		return wrap(vanilla, io());
	}

	public static ExecutorService worldgen() {
		ensure();
		return worldgen;
	}

	public static ExecutorService serialize() {
		ensure();
		return serialize;
	}

	public static ExecutorService io() {
		ensure();
		return io;
	}

	public static <T> Supplier<T> lockedSave(long chunkPos, Supplier<T> save) {
		return () -> {
			Object lock = writeLocks.computeIfAbsent(chunkPos, key -> new Object());
			synchronized (lock) {
				try {
					return save.get();
				} catch (RuntimeException thrown) {
					fallbackToVanilla(thrown);
					throw thrown;
				} finally {
					if (writeLocks.size() > 4096) {
						writeLocks.clear();
					}
				}
			}
		};
	}

	public static int[] noiseScratch() {
		return noiseScratch.get();
	}

	public static boolean isChunkWorker() {
		String name = Thread.currentThread().getName();
		return name.startsWith("vulkanperf-worldgen") || name.startsWith("vulkanperf-serialize") || name.startsWith("vulkanperf-chunkio");
	}

	public static boolean usingFallback() {
		return fallback.get();
	}

	private static Executor wrap(Executor vanilla, ExecutorService ours) {
		if (fallback.get()) {
			return vanilla;
		}
		return command -> ours.execute(() -> {
			try {
				command.run();
			} catch (Throwable thrown) {
				fallbackToVanilla(thrown);
				vanilla.execute(command);
			}
		});
	}

	private static void fallbackToVanilla(Throwable thrown) {
		if (fallback.compareAndSet(false, true)) {
			VulkanPerf.LOGGER.error("chunk worker failed; falling back to vanilla executors", thrown);
		}
	}

	private static void ensure() {
		if (worldgen == null) {
			start();
		}
	}

	private static ThreadFactory named(String prefix) {
		AtomicInteger n = new AtomicInteger();
		return runnable -> {
			Thread thread = new Thread(runnable, prefix + "-" + n.incrementAndGet());
			thread.setDaemon(true);
			return thread;
		};
	}
}
