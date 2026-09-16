# C2ME (RelativityMC / ishland) — full port inventory for vulkan-perf

## Recommended ref

| Item | Value |
| --- | --- |
| **Recommended** | Git branch **`dev/26.3.0`** (matches vulkan-perf MC 26.3) |
| Modrinth | `0.4.2-alpha.0.85+26.3` (alpha, 2026-09-15) |
| Alt | `dev/26.2.0` / `preview/26.2.0` if pinning 26.2 |
| Clone | `/workspace/ref/github/c2me` @ `dev/26.3.0` |
| Layout | Multi-module Fabric project (~20 feature jars) |
| Mixin count | **~200** across modules (+70 accessors in `c2me-base`) |
| License | **MIT** default; some dirs **All Rights Reserved** under `licenses/` — do not copy ARR trees; rewrite MIT-scope ideas only |

C2ME uses **Yarn** names in mixins. Table below maps to **Mojmap 26.3** (verified under `/tmp/mc-check`).

## Yarn → Mojmap cheat sheet (high traffic)

| Yarn (C2ME) | Mojmap 26.3 |
| --- | --- |
| `ServerChunkLoadingManager` | `net.minecraft.server.level.ChunkMap` |
| `ServerChunkManager` | `net.minecraft.server.level.ServerChunkCache` |
| `ChunkLevelManager` | `net.minecraft.server.level.DistanceManager` |
| `WorldChunk` | `net.minecraft.world.level.chunk.LevelChunk` |
| `World` / `ServerWorld` | `Level` / `ServerLevel` |
| `NoiseChunkGenerator` | `NoiseBasedChunkGenerator` |
| `ChunkNoiseSampler` | `NoiseChunk` |
| `NoiseConfig` | `RandomState` |
| `StorageIoWorker` | `IOWorker` |
| `RegionBasedStorage` | `RegionFileStorage` |
| `VersionedChunkStorage` | `SimpleRegionStorage` |
| `SerializedChunk` | `SerializableChunkData` |
| `SerializingRegionBasedStorage` | `SectionStorage` |
| `ServerLightingProvider` | `ThreadedLevelLightEngine` |
| `PointOfInterestStorage` | `PoiManager` |
| `ChunkRegion` | `WorldGenRegion` |
| `ServerEntityManager` | `PersistentEntitySectionManager` |
| `ChunkDataSender` | `PlayerChunkSender` |
| `SpawnHelper` | `NaturalSpawner` |
| `PlayerManager` | `PlayerList` |
| `NbtCompound` / `NbtList` | `CompoundTag` / `ListTag` |
| `Identifier` | `net.minecraft.resources.Identifier` (26.3 name) |
| `AquiferSampler.Impl` | `Aquifer.NoiseBasedAquifer` (was `Impl`) |
| `StructureWeightSampler` | `Beardifier` |
| `BiomeAccess` | `BiomeManager` |
| `GameOptions` | `Options` |
| `DistanceManager` nested ticket trackers | `DistanceManager$PlayerTicketTracker`, `$FixedPlayerDistanceChunkTracker`; simulation via `SimulationChunkTracker` |

## Impact ranking (TPS / chunk gen / IO)

Ranked for **full** C2ME-class gains. Modules marked ARR-sensitive should be designed from behavior notes only.

| # | Module | Key mixins / features | Mojmap targets + methods | Impact |
| --- | --- | --- | --- | --- |
| 1 | `c2me-rewrites-chunk-system` | `MixinThreadedAnvilChunkStorage`, `MixinServerChunkManager`, `MixinChunkHolder`, `MixinChunkLevelManager`, async serialization set | `ChunkMap` save/close/init; `ServerChunkCache#getChunk`, `#tick`, `#updateChunks`; `ChunkHolder`; `DistanceManager#update`; `SerializableChunkData#convert`/`fromChunk`; `IOWorker`; `ProtoChunk`; `WorldGenRegion#setBlockState`; `LevelChunk` post-process | **S** (architecture) |
| 2 | `c2me-opts-scheduling` | mid-tick chunk tasks, enhanced autosave, task scheduling, shutdown | `MinecraftServer`, `ServerChunkCache`, `ServerLevel`, `Level`, `ChunkMap#unloadChunks`, `ChunkHolder`, entity chunk data access | **S** TPS smoothness |
| 3 | `c2me-opts-dfc` | density function compiler / sampler caches | `NoiseChunk` (+ nested cache types if present), `RandomState.<init>`, `NoiseRouter`, spline `Implementation`, wrapping density functions | **S** worldgen CPU |
| 4 | `c2me-rewrites-chunkio` + `c2me-opts-chunkio` | async region IO, NBT cache limit, sync-write hiding | `IOWorker.<init>`, `SimpleRegionStorage`, `RegionFileStorage.<init>`, RecreationStorage | **S** IO wait |
| 5 | `c2me-threading-lighting` | parallel light | `ThreadedLevelLightEngine`, `ChunkMap.<init>`/`close` | **A–S** (w/ ScalableLux hooks) |
| 6 | `c2me-notickvd` | view distance ≠ simulation / tick distance | `DistanceManager`, `ChunkMap#setViewDistance`, `PlayerChunkSender`, `NaturalSpawner#createState`/`setupSpawn`, `PlayerList`, network options handlers, `LevelChunk#runPostProcessing` | **A–S** for high RD |
| 7 | `c2me-opts-worldgen-vanilla` | aquifer + beardifier + TL cache | `Aquifer.NoiseBasedAquifer`, `Beardifier`, `Block` thread-local | **A** gen |
| 8 | `c2me-opts-natives-math` | native biome/noise (needs JNI/clang) | `BiomeManager`, `Aquifer.NoiseBasedAquifer`, end islands sampler | **A** if natives built |
| 9 | `c2me-opts-allocs` | NBT/RL/predicate/ore pooling | `CompoundTag`, `ListTag`, `Identifier`, `Util`, `OreFeature#generateVeinPart`, block predicates | **A** GC |
| 10 | `c2me-rewrites-chunk-serializer` | faster serialize | `ChunkMap`, `ChunkStatus`, `Heightmap.Types`, `Identifier` | **A** |
| 11 | `c2me-fixes-worldgen-threading-issues` | structure/noise thread safety | many structure pieces, `NoiseBasedChunkGenerator`, `ServerLevel`, `LightStorage`, `StructureStart`, … | **required** for parallel gen correctness |
| 12 | `c2me-fixes-general-threading-issues` | async catchers / shutdown | `MinecraftServer`, `ServerChunkCache`, `ChunkMap` | required |
| 13 | `c2me-opts-worldgen-general` | random instance redirects | atomic simple random factory mixins | A |
| 14 | `c2me-client-uncapvd` | client VD uncap + fog | `Options`, `SyncedClientOptions`, `FogRenderer` | B client |
| 15 | `c2me-opts-accel-opencl` | OpenCL gen (optional/heavy) | `NoiseChunk`, `NoiseBasedChunkGenerator`, `PalettedContainer`, chunk system hooks | B optional |
| 16 | `c2me-opts-worldgen-biome-cache` | biome cache (module present) | biome source paths | A |
| 17 | `c2me-base` accessors | ~70 `I*` accessor mixins | ducks for all of the above | support |
| 18 | `c2me-server-utils` | commands | `Commands` / command manager | tooling |
| 19 | `c2me-fixes-chunkio-threading-issues` | `StructurePoolElement` | pool element thread fix | small |
| 20 | `c2me-fixes-worldgen-vanilla-bugs` | chunk status before callback | `ChunkHolder` | small |

### Top ~25 mixin *classes* to prioritize (rewrite targets)

1. `ChunkMap` — scheduling, save, VD, lighting ctor hooks  
2. `ServerChunkCache` — `getChunk` / `tick` / `updateChunks`  
3. `DistanceManager` (+ `PlayerTicketTracker`) — ticket/VD split  
4. `ChunkHolder` — task scheduling / serialization gates  
5. `IOWorker` — async store/load pipeline  
6. `SerializableChunkData` — async convert  
7. `NoiseBasedChunkGenerator#buildTerrain` / biome populate  
8. `NoiseChunk` — DFC cache sampling  
9. `RandomState` — noise config wiring  
10. `Aquifer.NoiseBasedAquifer`  
11. `Beardifier`  
12. `ThreadedLevelLightEngine`  
13. `MinecraftServer` — mid-tick tasks, autosave, shutdown  
14. `ServerLevel` / `Level` — mid-tick + setBlock gates  
15. `LevelChunk` — post-process / tickability  
16. `RegionFileStorage` / `SimpleRegionStorage`  
17. `SectionStorage` / `PoiManager`  
18. `WorldGenRegion`  
19. `PlayerChunkSender`  
20. `NaturalSpawner`  
21. `CompoundTag` / `ListTag` alloc  
22. `BiomeManager` (natives)  
23. `OreFeature` pooling  
24. `ProtoChunk` / `Blender` async serialization safety  
25. `PersistentEntitySectionManager` shutdown/fixes  

## What already exists in vulkan-perf

README: **`chunks` is NOT C2ME-class**; C2ME allowed alongside.

| Existing file | Behavior | vs C2ME |
| --- | --- | --- |
| `chunks/ChunkWorkers.java` | local thread pools | tiny subset of scheduling |
| `mixin/chunks/ChunkGeneratorMixin` → `ChunkGenerator#createBiomes` | redirect executor | not parallel chunk system |
| `mixin/chunks/NoiseBasedChunkGeneratorMixin` → `#buildTerrain` | redirect executor | no DFC / aquifer / safety |
| `mixin/chunks/IoWorkerMixin` → `IOWorker.<init>` | thread pool | not full async IO rewrite |
| `mixin/chunks/ChunkMapIoMixin` → `ChunkMap#save` | redirect | not rewrite serializer/system |

**Missing:** chunk system rewrite, DFC, notick VD, lighting threading, worldgen thread fixes, natives, OpenCL, serializer rewrite, alloc suite.

## Concrete files to create (`dev.vulkanperf.*`)

Suggest new top-level module name `c2me` or expand `chunks` into subpackages. Prefer **`dev.vulkanperf.chunksys`** to avoid trademark confusion while documenting “C2ME-class”.

### Config

```
PerfConfig.ChunksConfig → expand or add ChunkSysConfig:
  enabled
  rewriteChunkSystem
  asyncIo
  densityFunctionOpts
  midTickScheduling
  enhancedAutosave
  noTickViewDistance
  threadedLighting
  worldgenVanillaOpts
  nativesMath
  allocOpts
  clientUncapVd
```

### Support / engines

```
src/main/java/dev/vulkanperf/chunksys/ChunkSystem.java
src/main/java/dev/vulkanperf/chunksys/ChunkTaskScheduler.java
src/main/java/dev/vulkanperf/chunksys/MidTickChunkRunner.java
src/main/java/dev/vulkanperf/chunksys/AsyncChunkSerializer.java
src/main/java/dev/vulkanperf/chunksys/io/AsyncRegionIo.java
src/main/java/dev/vulkanperf/chunksys/io/NbtCacheLimiter.java
src/main/java/dev/vulkanperf/chunksys/dfc/DensityFunctionCompiler.java
src/main/java/dev/vulkanperf/chunksys/dfc/NoiseChunkCaches.java
src/main/java/dev/vulkanperf/chunksys/vd/ViewDistanceController.java
src/main/java/dev/vulkanperf/chunksys/light/LightingExecutor.java
src/main/java/dev/vulkanperf/chunksys/gen/AquiferOptimizer.java
src/main/java/dev/vulkanperf/chunksys/gen/BeardifierOptimizer.java
src/main/java/dev/vulkanperf/chunksys/alloc/TagPooling.java
src/main/java/dev/vulkanperf/chunksys/thread/WorldgenThreadSafety.java
# keep / evolve:
src/main/java/dev/vulkanperf/chunks/ChunkWorkers.java
```

### Mixins (initial FULL slice file list)

```
src/main/java/dev/vulkanperf/mixin/chunksys/ChunkMapSystemMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/ChunkMapSaveMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/ServerChunkCacheMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/DistanceManagerMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/PlayerTicketTrackerMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/ChunkHolderMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/MinecraftServerSchedulingMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/ServerLevelMidTickMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/LevelMidTickMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/IOWorkerAsyncMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/RegionFileStorageMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/SimpleRegionStorageMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/SerializableChunkDataMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/NoiseBasedChunkGeneratorMixin.java   # replace thin redirect
src/main/java/dev/vulkanperf/mixin/chunksys/NoiseChunkDfcMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/RandomStateMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/AquiferNoiseBasedMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/BeardifierMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/ThreadedLevelLightEngineMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/LevelChunkPostProcessMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/WorldGenRegionMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/PlayerChunkSenderMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/NaturalSpawnerMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/CompoundTagAllocMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/ListTagAllocMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/PoiManagerMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/SectionStorageMixin.java
src/main/java/dev/vulkanperf/mixin/chunksys/fixes/StructureThreadingMixins.java  # split per structure as needed
src/client/java/dev/vulkanperf/client/mixin/chunksys/OptionsViewDistanceMixin.java
src/client/java/dev/vulkanperf/client/mixin/chunksys/FogRendererVdMixin.java
```

Plus accessor interfaces under `dev.vulkanperf.chunksys.access.*` mirroring `c2me-base` needs (only those actually used).

## Compile / scope risks

1. **Largest invasive port** — touches chunk lifetime; needs long soak tests.  
2. Yarn→Mojmap + 26.3 renames (`Aquifer.NoiseBasedAquifer`, `Identifier`).  
3. Nested DFC cache class names may differ from older Yarn `Cache2D`/`CellCache` — discover inside `NoiseChunk` / density function visitors in 26.3 sources before writing mixins.  
4. Natives/OpenCL need toolchain (README: JDK25+, Clang, CMake) — gate optional.  
5. ARR-licensed C2ME subtrees: **do not copy**; reimplement from public behavior/docs.  
6. Soft-conflict: today C2ME can co-install; FULL port should eventually break/replace like Lithium plan.

## Port strategy

1. Harden current `ChunkWorkers` redirects (already there).  
2. Scheduling + async IO + serializer safety (visible TPS/IO).  
3. DFC + aquifer/beardifier (gen throughput).  
4. Threading fixes pack (correctness).  
5. NoTick VD + lighting threading.  
6. Allocs + natives optional.  
7. Only then claim C2ME replacement in README.
EOF