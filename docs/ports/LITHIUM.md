# Lithium (CaffeineMC) — full port inventory for vulkan-perf

## Recommended ref

| Item | Value |
| --- | --- |
| **Recommended** | Git tag **`mc26.3-0.26.0`** (Modrinth `mc26.3-0.26.0-fabric`, alpha 2026-09-15) |
| Alt (stable) | Tag `mc26.2-0.25.3` if staying on 26.2 |
| Clone | `/workspace/ref/github/lithium` @ `mc26.3-0.26.0` |
| Upstream package | `net.caffeinemc.mods.lithium.mixin.*` |
| Mixins listed | **286** in `lithium.mixins.json` (~447 Java files under mixin/) |
| License | **LGPL-3.0** → rewrite only, no paste |

MC version in tag matches vulkan-perf (`minecraft_version=26.3`). Prefer this over `26.2` branch.

## Impact ranking (TPS-first, then client)

Ranked by typical server TPS / entity-heavy FPS impact. Feature module = Lithium mixin package prefix.

### Tier S — always port first (huge TPS)

| # | Feature module | Key mixin classes | Mojmap 26.3 class + method(s) |
| --- | --- | --- | --- |
| 1 | `entity.collisions.movement` | `EntityMixin` | `net.minecraft.world.entity.Entity#collide(Vec3)` (Overwrite) |
| 2 | `entity.fast_retrieval` | `EntitySectionStorageMixin` | `net.minecraft.world.level.entity.EntitySectionStorage#forEachAccessibleNonEmptySection` |
| 3 | `entity.collisions.intersection` | `EntityGetterMixin`, `LevelMixin` | `EntityGetter#getEntityCollisions(Entity,AABB)`; `Level` entity queries |
| 4 | `block.hopper` (+ sleeping) | `HopperBlockEntityMixin`, `HopperBlockMixin`, idle/sleep mixins | `HopperBlockEntity#ejectItems`, `#suckInItems`, `#tryMoveItems`, `#pushItemsTick`, `#setCooldown` |
| 5 | `world.block_entity_ticking.sleeping` | `LevelChunkMixin`, `BlockEntityMixin`, furnace/hopper/brewing/campfire/… | `LevelChunk` ticker lambda; `BlockEntity#setChanged`; per-BE `serverTick` / `cookTick` |
| 6 | `shapes.optimized_matching` / `shape_merging` / `specialized_shapes` | `ShapesMixin`, `VoxelShapeMixin` | `Shapes#joinIsNotEmpty`, `#createIndexMerger`, `#or`/`#join`; `VoxelShape` hot paths |
| 7 | `ai.pathing` | `WalkNodeEvaluatorMixin`, `PathNavigationRegionMixin`, `BlockStateBaseMixin` | `WalkNodeEvaluator#getPathTypeFromState`, `#checkNeighbourBlocks`; `PathNavigationRegion`; `BlockBehaviour.BlockStateBase` path type cache |
| 8 | `entity.inactive_navigations` | `PathNavigationMixin`, `ServerLevelMixin`, `MobMixin` | `PathNavigation#recomputePath`, `#moveTo`, `#stop`; `ServerLevel#sendBlockUpdated` |
| 9 | `collections.entity_filtering` / `entity_by_type` | `ClassInstanceMultiMapMixin` | `net.minecraft.util.ClassInstanceMultiMap` (Overwrite find/filter) |
| 10 | `world.chunk_ticking.random_block_ticking` | `LevelChunkSectionMixin`, `ServerLevelMixin` | `LevelChunkSection#recalcBlockCounts`, `#setBlockState`; `ServerLevel` random tick |

### Tier A — high TPS / common farms

| # | Feature module | Key mixins | Mojmap target |
| --- | --- | --- | --- |
| 11 | `ai.poi` (+ `fast_portals`) | `PoiManagerMixin`, `PoiSectionMixin`, `SectionStorageMixin`, `PortalForcerMixin` | `PoiManager`, `PoiSection`, `SectionStorage`, `PortalForcer` |
| 12 | `ai.sensor` / `ai.task` / `useless_*` | Brain sensor/task stream replacements | `Brain`, `Sensor`, `Behavior` subclasses; `Brain#tick` |
| 13 | `collections.brain` | `BrainMixin` | `Brain.<init>` / memory maps |
| 14 | `world.raycast` | `BlockGetterMixin` | `BlockGetter` clip/ray Overwrite |
| 15 | `world.explosions.*` | `ServerExplosionMixin`, `ClipContextMixin` | `ServerExplosion#calculateExplodedPositions`, `#getSeenPercent`, `#explode` |
| 16 | `world.tick_scheduler` | `LevelChunkTicksMixin` | `LevelChunkTicks` (Overwrite) |
| 17 | `chunk.palette` / `chunk.no_locking` | `PalettedContainerMixin`, `LevelChunkSectionMixin`, `StrategyMixin` | `PalettedContainer#acquire`/`#release`/`getAndSet`; `LevelChunkSection#setBlockState` |
| 18 | `experimental.entity.item_entity_merging` | `ItemEntityMixin` | `ItemEntity#mergeWithNeighbours` |
| 19 | `experimental.entity.block_caching.*` | `EntityMixin` (support/suffocation) | `Entity#checkSupportingBlock`, `#isInWall` |
| 20 | `alloc.*` (nbt, entity_iteration, chunk_random, enum_values) | various | `CompoundTag`, `EntitySection#getEntities`, `ServerLevel#tickChunk`, piston/redstone enum loops |
| 21 | `math.fast_blockpos` / `math.fast_util` / `math.sine_lut` | `BlockPosMixin`, `AABBMixin`, `DirectionMixin`, `MthMixin` | `BlockPos`, `AABB`, `Direction`, `Mth` (Overwrite hot helpers) |
| 22 | `world.combined_heightmap_update` | `LevelChunkMixin` | `LevelChunk#setBlockState(BlockPos,BlockState,int)` |
| 23 | `block.redstone_wire` / `block.fluid.flow` | `RedstoneWireBlockMixin`, `FlowingFluidMixin` | `RedstoneWireBlock` / `DefaultRedstoneWireEvaluator#calculateTargetStrength`; `FlowingFluid#getSpread` |
| 24 | `world.game_events.dispatch` | `GameEventDispatcherMixin`, `LevelChunkMixin` | `GameEventDispatcher#post` |
| 25 | `gen.cached_generator_settings` | `NoiseBasedChunkGeneratorMixin` | `NoiseBasedChunkGenerator` settings cache (Overwrite accessor) |

### Tier B — solid wins / client TPS

| # | Feature module | Notes / Mojmap |
| --- | --- | --- |
| 26 | `client_tick.entity.*` / `client_tick.particle` | `Mob`, `LivingEntity`, `Brain`, `ClientLevel#doAnimateTick`, `AmbientParticle` |
| 27 | `entity.equipment_tracking.*` | `LivingEntity#collectEquipmentChanges`, `#detectEquipmentUpdates`; `EntityEquipment` |
| 28 | `collections.block_entity_tickers` / `entity_ticking` / `mob_spawning` | `LevelChunk`, `EntityTickList`, `MobSpawnSettings`, `WeightedList` |
| 29 | `minimal_nonvanilla.spawning` | `NaturalSpawner#createState`; `EntitySectionStorage` |
| 30 | `world.chunk_ticking.precipitation` / `spread_ice` | `ServerLevel#tickPrecipitation`; `Biome#shouldFreeze` |

Skip / defer for first FULL slice: `debug.*`, `compat.worldedit`, `profiler`, most `util.accessors` (needed as support, not FPS), `startup`.

## What already exists in vulkan-perf

Config: `PerfConfig.LogicConfig` + `MemoryConfig.internShapes`. README claims Lithium **partial**.

| Existing file | Covers Lithium area | Gap vs upstream |
| --- | --- | --- |
| `mixin/logic/LevelCollisionMixin` → `Level#getEntities` | collision entity query cache | Not full `Entity#collide` / section retrieval |
| `mixin/logic/HopperBlockEntityMixin` | hopper container lookup cache | Missing eject/suck Overwrites + inventory listeners |
| `mixin/logic/HopperIdleMixin` → `pushItemsTick` | hopper idle skip | Not full sleeping BE system |
| `mixin/logic/BrainMixin` → `Brain#tick` | inactive AI throttle | Not sensor/task stream opts |
| `mixin/logic/ShapesJoinMixin` → `Shapes#or` | shape OR cache | Missing `joinIsNotEmpty` / specialized shapes |
| `mixin/memory/ShapesJoinCacheMixin` → `Shapes#join` | Ferrite-ish interning | Partial shape memory only |
| `mixin/logic/PathNavigationMixin` → `createPath` | path cache | Not inactive navigation / node evaluator |
| `mixin/logic/ItemEntityMixin` → `mergeWithNeighbours` | item merge | Experimental Lithium parity thin |
| `mixin/logic/MobAiMixin` → `Mob#serverAiStep` | mob AI skip | Not equipment/pathing stacks |
| `logic/EntitySectionIndex` | support for entity queries | Not Lithium `fast_retrieval` rewrite |

**Missing entirely:** sleeping block entities, POI, explosions, raycast, tick scheduler, palette/no_locking, redstone/fluid, heightmap combine, math Overwrites, collections rewrites, client_tick, most AI.

## Concrete files to create (`dev.vulkanperf.*`)

Rewrite under new packages; keep feature flags in `PerfConfig`. Suggested layout:

### Config / plugin

- `src/main/java/dev/vulkanperf/config/PerfConfig.java` — extend with `LogicConfig` subflags (or new `LithiumConfig` nested)
- `src/main/java/dev/vulkanperf/mixin/VulkanPerfMixinPlugin.java` — gate new packages
- `src/main/resources/vulkanperf.mixins.json` — register mixins

### Support (non-mixin)

- `src/main/java/dev/vulkanperf/logic/collision/EntityCollisionEngine.java`
- `src/main/java/dev/vulkanperf/logic/hopper/HopperInventoryCache.java`
- `src/main/java/dev/vulkanperf/logic/hopper/HopperSleepController.java`
- `src/main/java/dev/vulkanperf/logic/shapes/ShapeJoinCache.java` (merge with memory module)
- `src/main/java/dev/vulkanperf/logic/shapes/SpecializedShapes.java`
- `src/main/java/dev/vulkanperf/logic/pathing/PathTypeCache.java`
- `src/main/java/dev/vulkanperf/logic/pathing/InactiveNavigationTracker.java`
- `src/main/java/dev/vulkanperf/logic/ai/PoiFastIndex.java`
- `src/main/java/dev/vulkanperf/logic/sleeping/BlockEntitySleep.java`
- `src/main/java/dev/vulkanperf/logic/collections/TypeFilteredEntityMap.java`
- `src/main/java/dev/vulkanperf/logic/alloc/EnumDirectionCache.java`

### Mixins — Tier S/A file list

```
src/main/java/dev/vulkanperf/mixin/logic/collision/EntityCollideMixin.java
src/main/java/dev/vulkanperf/mixin/logic/collision/EntityGetterCollisionsMixin.java
src/main/java/dev/vulkanperf/mixin/logic/collision/EntitySectionStorageMixin.java
src/main/java/dev/vulkanperf/mixin/logic/collision/EntitySectionMixin.java
src/main/java/dev/vulkanperf/mixin/logic/hopper/HopperEjectSuckMixin.java
src/main/java/dev/vulkanperf/mixin/logic/hopper/HopperBlockMixin.java
src/main/java/dev/vulkanperf/mixin/logic/hopper/HopperSleepMixin.java
src/main/java/dev/vulkanperf/mixin/logic/sleeping/LevelChunkTickerMixin.java
src/main/java/dev/vulkanperf/mixin/logic/sleeping/BlockEntityChangedMixin.java
src/main/java/dev/vulkanperf/mixin/logic/sleeping/AbstractFurnaceSleepMixin.java
src/main/java/dev/vulkanperf/mixin/logic/sleeping/BrewingStandSleepMixin.java
src/main/java/dev/vulkanperf/mixin/logic/sleeping/CampfireSleepMixin.java
src/main/java/dev/vulkanperf/mixin/logic/shapes/ShapesJoinIsNotEmptyMixin.java
src/main/java/dev/vulkanperf/mixin/logic/shapes/ShapesIndexMergerMixin.java
src/main/java/dev/vulkanperf/mixin/logic/shapes/VoxelShapeSpecializedMixin.java
src/main/java/dev/vulkanperf/mixin/logic/pathing/WalkNodeEvaluatorMixin.java
src/main/java/dev/vulkanperf/mixin/logic/pathing/PathNavigationRegionMixin.java
src/main/java/dev/vulkanperf/mixin/logic/pathing/BlockStatePathCacheMixin.java
src/main/java/dev/vulkanperf/mixin/logic/pathing/InactivePathNavigationMixin.java
src/main/java/dev/vulkanperf/mixin/logic/pathing/ServerLevelNavInvalidateMixin.java
src/main/java/dev/vulkanperf/mixin/logic/collections/ClassInstanceMultiMapMixin.java
src/main/java/dev/vulkanperf/mixin/logic/chunk/LevelChunkSectionRandomTickMixin.java
src/main/java/dev/vulkanperf/mixin/logic/chunk/PalettedContainerNoLockMixin.java
src/main/java/dev/vulkanperf/mixin/logic/poi/PoiManagerMixin.java
src/main/java/dev/vulkanperf/mixin/logic/poi/PoiSectionMixin.java
src/main/java/dev/vulkanperf/mixin/logic/world/BlockGetterRaycastMixin.java
src/main/java/dev/vulkanperf/mixin/logic/world/ServerExplosionMixin.java
src/main/java/dev/vulkanperf/mixin/logic/world/LevelChunkTicksMixin.java
src/main/java/dev/vulkanperf/mixin/logic/world/LevelChunkHeightmapMixin.java
src/main/java/dev/vulkanperf/mixin/logic/redstone/RedstoneWireEvaluatorMixin.java
src/main/java/dev/vulkanperf/mixin/logic/fluid/FlowingFluidSpreadMixin.java
src/main/java/dev/vulkanperf/mixin/logic/math/BlockPosFastMixin.java
src/main/java/dev/vulkanperf/mixin/logic/math/AabbFastMixin.java
src/main/java/dev/vulkanperf/mixin/logic/math/MthSineMixin.java
src/client/java/dev/vulkanperf/client/mixin/logic/ClientLevelParticleTickMixin.java
src/client/java/dev/vulkanperf/client/mixin/logic/ClientUnusedBrainMixin.java
```

Expand sleeping BE mixins 1:1 with Lithium’s sleeping package once Tier S lands.

## Compile notes (Mojmap 26.3)

- `BlockStateBase` is nested: `BlockBehaviour.BlockStateBase` (`initCache` lives here).
- `IOWorker` spelling (not `IoWorker`) — already used by vulkan-perf chunks.
- Prefer Inject/Redirect over Overwrite where Lithium Overwrites; document any unavoidable Overwrite.
- Lithium `common` is Yarn/Mojmap-ish already on 26.3 tag — good reference for signatures only.

## Port strategy

1. Expand `logic` flags; do **not** soft-conflict break Lithium until parity checklist passes.
2. Implement Tier S → A → B; microbench entity collision + hopper + shape join.
3. After FULL parity, set mod-break metadata / README row to “replaces Lithium”.
EOF