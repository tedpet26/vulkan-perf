package dev.vulkanperf.chunksys;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * C2ME-style mid-tick chunk task interleaving: vanilla only drains the chunk
 * main-thread queue when the whole server has spare time; a heavy tick can
 * starve chunk load/light callbacks for the entire tick. Draining a bounded
 * batch after each level tick keeps chunk delivery smooth. Throttled to a
 * configurable nanos interval so idle servers pay nothing.
 */
public final class MidTickChunkTasks {
	private static final Map<ServerLevel, long[]> LAST_RUN = new ConcurrentHashMap<>();

	private MidTickChunkTasks() {
	}

	public static void runMidTick(ServerLevel level) {
		PerfConfig.ChunksConfig config = PerfConfig.get().chunks;
		if (!config.enabled || !config.midTickScheduling) {
			return;
		}
		long now = System.nanoTime();
		long[] last = LAST_RUN.computeIfAbsent(level, key -> new long[1]);
		if (now - last[0] < config.midTickIntervalNanos) {
			return;
		}
		last[0] = now;
		try {
			// Bounded opportunistic drain of the chunk source main-thread queue.
			level.getChunkSource().pollTask();
		} catch (Throwable ignored) {
			// Never let the opportunistic drain disturb the tick.
		}
	}
}
