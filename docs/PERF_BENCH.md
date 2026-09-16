# Performance comparison notes (Phase C)

## Build / compile

```
JAVA_HOME=<jdk-25> ./gradlew build
```

Result on this agent: **BUILD SUCCESSFUL** (Java 25 / Loom 1.17.19 / MC 26.3).

## Offline microbench

```
javac -d /tmp/vp-bench src/main/java/dev/vulkanperf/bench/CacheMicrobench.java
java -cp /tmp/vp-bench dev.vulkanperf.bench.CacheMicrobench
```

Measures the cache shapes used by Ferrite-style join interning and entity
occlusion tick caches (not full-frame FPS).

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
