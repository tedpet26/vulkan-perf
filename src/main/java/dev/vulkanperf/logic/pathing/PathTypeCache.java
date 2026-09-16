package dev.vulkanperf.logic.pathing;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared per-BlockState path-type cache (Lithium ai.pathing style): mob path
 * evaluators repeatedly derive the same PathType from identical BlockState
 * instances, so memoize on state identity.
 */
public final class PathTypeCache {
	private static final ConcurrentHashMap<BlockState, PathType> CACHE = new ConcurrentHashMap<>();

	private PathTypeCache() {
	}

	public static PathType get(BlockState state) {
		return CACHE.get(state);
	}

	public static void put(BlockState state, PathType type) {
		if (CACHE.size() > 65536) {
			CACHE.clear();
		}
		CACHE.put(state, type);
	}

	public static void clear() {
		CACHE.clear();
	}
}
