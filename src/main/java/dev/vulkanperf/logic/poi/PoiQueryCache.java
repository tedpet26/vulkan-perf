package dev.vulkanperf.logic.poi;

import net.minecraft.core.BlockPos;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Single-tick memo for {@code PoiManager#findClosest} results. AI sensors ask
 * the same question many times per tick; the answer cannot change within a tick
 * because POI data only mutates on block updates which bump the stamp.
 */
public final class PoiQueryCache {
	private static final int MAX = 1024;
	private static final ConcurrentHashMap<QueryKey, Object> CACHE = new ConcurrentHashMap<>();
	private static final AtomicLong STAMP = new AtomicLong();

	private PoiQueryCache() {
	}

	public static void beginTick() {
		STAMP.incrementAndGet();
		if (STAMP.get() % 32 == 0 && CACHE.size() > MAX) {
			CACHE.clear();
		}
	}

	public static Object get(PredicateBox predicate, BlockPos center, int radius, Object occupancy) {
		return CACHE.get(new QueryKey(predicate, center.asLong(), radius, occupancy, STAMP.get()));
	}

	public static void put(PredicateBox predicate, BlockPos center, int radius, Object occupancy, Object result) {
		if (CACHE.size() > MAX * 2) {
			CACHE.clear();
		}
		CACHE.put(new QueryKey(predicate, center.asLong(), radius, occupancy, STAMP.get()), result);
	}

	/** Opaque wrapper so mixins can pass predicates without equality claims. */
	public interface PredicateBox {
		Object raw();
	}

	private record QueryKey(PredicateBox predicate, long center, int radius, Object occupancy, long tick) {
	}
}
