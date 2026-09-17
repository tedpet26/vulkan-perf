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

## Spark standalone-agent profiles (2026-09-16)

Since no spark build targets 26.3, the standalone agent (spark-1.10.185,
`-javaagent:spark-...-standalone-agent.jar=port=2223`) was attached to the
production 26.3 Prism instance and driven over its SSH interface. Identical
workload both runs: fresh superflat world (seed `vulkanperf`), forceload of
256 chunks, 120 AI villagers + 40 armor stands, camera panning, F3 overlay on.

- Clean run: https://spark.lucko.me/iY3yyNYNkk
- Warm-up run (natural spawning, no bench entities): https://spark.lucko.me/eSoUJvxoYO

Recorded observations (client: Ryzen 7 5800X, RX 7900 XTX, Vulkan 1.4.344,
maxFps 260, vsync off, RD 12):

- Render thread ~1.2k FPS focused; biggest *active* cost after idle-park is
  the GUI/text pipeline: `GuiRenderer.addElementToMesh`,
  `GuiRenderState.hasIntersection`, `Font$PreparedTextBuilder`,
  `BakedSheetGlyph drawFast`, ICU Bidi — together ~10-15% of busy time.
  `hasIntersection` alone scans every element of a node list per added glyph.
- Server thread 75-88% parked (client ticks 6-16 ms of the 50 ms budget);
  hottest self-times: villager `Brain.startEachNonRunningBehavior` streams,
  `FluidState.isRandomlyTicking`, `BlockCollisions`, `PalettedContainer.get`.
- vulkanperf worker pools (chunkio/serialize/worldgen) fully idle on a flat
  world — no regression risk there for this workload.
- One hard crash observed after the profile finished, sitting unfocused on a
  second GPU context: `IllegalStateException: 5s timeout reached when waiting
  for VK semaphore` (driver-level; not reproduced under normal play).

Changes driven by these profiles (same day): `imfast.guiIntersectionFastPath`
(per-list bounds union so `hasIntersection` rejects in O(1) when the new rect
cannot touch anything in the list) and `logic.fluidRandomTickCache` (cache
`FluidState#isRandomlyTicking` per state instance). Both default on, gated in
the mixin plugin, and toggleable in the Sodium-style config pages.

A matching spark profile of the 26.2 Prism instance (full individual-mod
stack: Sodium/Lithium/EntityCulling/FerriteCore/ImmediatelyFast/C2ME, plus
the Hypixel client mods) was attempted twice but the JVM died mid-profile
both times with `g1HeapRegionManager.cpp:55` internal errors (JDK 25.0.1);
that instance directory already holds several pre-existing `hs_err_pid*`
files from normal play, so the machine/JDK combination is crash-prone
independent of this mod. No uploaded 26.2 profile exists yet.
