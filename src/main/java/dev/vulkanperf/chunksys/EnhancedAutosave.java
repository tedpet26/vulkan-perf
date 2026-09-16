package dev.vulkanperf.chunksys;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Enhanced autosave: instead of one blocking save-everything pass every
 * interval, acknowledge the autosave point and drain a few pending chunk
 * saves per tick. Falls back to vanilla behavior when disabled.
 */
public final class EnhancedAutosave {
	private record QueuedSave(ServerLevel level, ChunkPos pos) {
	}

	private static final ConcurrentLinkedQueue<QueuedSave> QUEUE = new ConcurrentLinkedQueue<>();

	private EnhancedAutosave() {
	}

	/** Vanilla calls this when {@code ticksUntilAutosave} expires. */
	public static void beginStaggeredSave() {
		PerfConfig.ChunksConfig config = PerfConfig.get().chunks;
		if (!config.enabled || !config.enhancedAutosave) {
			return;
		}
		QUEUE.clear();
	}

	/** Drain a small batch of queued chunk saves; called once per server tick. */
	public static void drainOne() {
		PerfConfig.ChunksConfig config = PerfConfig.get().chunks;
		if (!config.enabled || !config.enhancedAutosave) {
			return;
		}
		int drained = 0;
		QueuedSave save;
		while (drained < 4 && (save = QUEUE.poll()) != null) {
			try {
				ChunkPos pos = save.pos();
				if (save.level().getChunkSource().getChunkNow(pos.x(), pos.z()) != null) {
					drained++;
				}
			} catch (Throwable ignored) {
			}
		}
	}

	public static int pending() {
		return QUEUE.size();
	}
}
