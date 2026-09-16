# Performance comparison notes (Phase C)

## Build / compile

```
JAVA_HOME=<jdk-25> ./gradlew build
```

Result on this agent: **BUILD SUCCESSFUL** (Java 25 / Loom 1.17.19 / MC 26.3).
Re-verified 2026-09-16 after the audit-residual cleanup: green build with the
packets module default-on, dead mixins removed, and the async entity occlusion
culler in place.

## Offline microbench

```
javac -d /tmp/vp-bench src/main/java/dev/vulkanperf/bench/CacheMicrobench.java
java -cp /tmp/vp-bench dev.vulkanperf.bench.CacheMicrobench
```

Measures the cache shapes used by Ferrite-style join interning and entity
occlusion tick caches (not full-frame FPS).

Recorded 2026-09-16 (Liberica JDK 25, Windows):

| Benchmark | Cold-ish run (200k ops) | Warm JIT (1M ops) |
| --- | --- | --- |
| shape-join-cache (`ConcurrentHashMap` intern shape) | 443 ns/op, size 8704 | 77 ns/op |
| entity-occl-cache (tick-cache lookup) | 7 ns/op | 3 ns/op |

Interpretation: the occlusion tick-cache path is effectively free per lookup
(single-digit ns), and the join-intern path costs ~77 ns/op warm against a
513-key working set — both far below one game-tick budget, so the cache layers
themselves are not the bottleneck; full-frame gains depend on the work they
avoid (raycasts, shape joins), which only the in-game matrix below measures.

## In-game comparison (manual)

Use the **same** world/seed, render distance, and JVM flags for each row:

| Setup | Expected vs Sodium alone |
| --- | --- |
| Sodium only | baseline |
| Sodium + Lithium + Entity Culling (+ optional C2ME) | highest TPS/FPS for crowded scenes |
| vulkan-perf (this PR) alone | should beat Sodium-only on entity-heavy and hopper-heavy scenes; still below full C2ME for chunk gen |
| vulkan-perf + C2ME | allowed now (C2ME no longer broken); best chunk load |

Metrics to record: F3 FPS, MSPT (`/tick`), entity count, hopper transfer
MSPT. Entity occlusion gains show most with many mobs behind walls.

**Status 2026-09-16: not yet recorded.** The rows above need an interactive
client with the upstream mods installed (Lithium/Entity Culling/C2ME on 26.3);
the offline environment used for development has no display and no second mod
stack. Until a human runs the matrix, the only recorded numbers are the build
status and the microbench above — do not cite FPS/TPS figures from this file.
