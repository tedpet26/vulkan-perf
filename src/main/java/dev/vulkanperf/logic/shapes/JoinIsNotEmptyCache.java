package dev.vulkanperf.logic.shapes;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches {@code Shapes#joinIsNotEmpty} decisions for identical shape/op triples.
 * Occlusion and collision checks call this constantly with the same few shape pairs.
 */
public final class JoinIsNotEmptyCache {
	private static final int MAX = 2048;
	private static final ConcurrentHashMap<Key, Boolean> CACHE = new ConcurrentHashMap<>();

	private JoinIsNotEmptyCache() {
	}

	public static boolean get(Object a, Object b, Object op) {
		return CACHE.get(new Key(a, b, op));
	}

	public static void put(Object a, Object b, Object op, boolean result) {
		if (CACHE.size() > MAX) {
			CACHE.clear();
		}
		CACHE.put(new Key(a, b, op), result);
	}

	public static int size() {
		return CACHE.size();
	}

	private record Key(Object a, Object b, Object op) {
	}
}
