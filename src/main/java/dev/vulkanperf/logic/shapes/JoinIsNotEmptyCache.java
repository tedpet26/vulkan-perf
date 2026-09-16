package dev.vulkanperf.logic.shapes;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches {@code Shapes#joinIsNotEmpty} decisions for identical shape/op triples.
 * Occlusion and collision checks call this constantly with the same few shape pairs.
 *
 * <p><strong>Bootstrap contract:</strong> {@link #get} must return {@code null} on a
 * miss and never throw. This handler runs during {@code Blocks}' static
 * initialization (via vanilla's {@code SHAPE_FULL_BLOCK_CACHE}, which feeds
 * {@code Shapes#joinIsNotEmpty} back into itself for weakly-reachable keys), and a
 * primitive unwrap of a missing entry there crashes the game with an NPE before the
 * world exists. Never "optimize" the boxed return into an auto-unboxed
 * {@code boolean}.
 */
public final class JoinIsNotEmptyCache {
	private static final int MAX = 2048;
	private static final ConcurrentHashMap<Key, Boolean> CACHE = new ConcurrentHashMap<>();

	private JoinIsNotEmptyCache() {
	}

	public static Boolean get(Object a, Object b, Object op) {
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
