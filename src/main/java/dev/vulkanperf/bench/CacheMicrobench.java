package dev.vulkanperf.bench;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Offline microbench for hot cache paths (no Minecraft runtime required).
 * Run: {@code java dev.vulkanperf.bench.CacheMicrobench}
 */
public final class CacheMicrobench {
	private CacheMicrobench() {
	}

	public static void main(String[] args) {
		int n = args.length > 0 ? Integer.parseInt(args[0]) : 200_000;
		benchShapeJoinCache(n);
		benchEntityCacheLookup(n);
		System.out.println("ok");
	}

	private static void benchShapeJoinCache(int n) {
		ConcurrentHashMap<Long, Object> cache = new ConcurrentHashMap<>();
		Object sentinel = new Object();
		long t0 = System.nanoTime();
		for (int i = 0; i < n; i++) {
			long key = (i % 512L) | ((i % 17L) << 32);
			cache.computeIfAbsent(key, k -> sentinel);
		}
		long t1 = System.nanoTime();
		System.out.printf("shape-join-cache: %d ops in %.2f ms (%.0f ns/op) size=%d%n",
			n, (t1 - t0) / 1e6, (t1 - t0) / (double) n, cache.size());
	}

	private static void benchEntityCacheLookup(int n) {
		int[] ids = new int[1024];
		boolean[] visible = new boolean[1024];
		long[] tick = new long[1024];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = i;
			visible[i] = (i & 1) == 0;
			tick[i] = i;
		}
		long hits = 0;
		long t0 = System.nanoTime();
		for (int i = 0; i < n; i++) {
			int id = i & 1023;
			if (tick[id] + 10 > i && visible[id]) {
				hits++;
			}
		}
		long t1 = System.nanoTime();
		System.out.printf("entity-occl-cache: %d ops in %.2f ms (%.0f ns/op) hits=%d%n",
			n, (t1 - t0) / 1e6, (t1 - t0) / (double) n, hits);
	}
}
