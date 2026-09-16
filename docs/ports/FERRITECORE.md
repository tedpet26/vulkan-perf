# FerriteCore (malte0811) — full port inventory for vulkan-perf

## Recommended ref

| Item | Value |
| --- | --- |
| **Recommended** | Git branch **`26.1`** / tag **`build-9.0.0`** (Modrinth `9.0.0-fabric`, game versions **26.1–26.2**) |
| **26.3 status** | No dedicated 26.3 branch yet; APIs below verified present in Mojmap **26.3** sources |
| Clone | `/workspace/ref/github/ferritecore` @ `26.1` / `build-9.0.0` |
| Upstream packages | `malte0811.ferritecore.mixin.{fastmap,blockstatecache,datacomponents,threaddetec,accessors}` |
| License | **MIT** → still rewrite only per project policy |

FerriteCore is **memory-first** (RSS / allocation), with secondary TPS gains from better locality and less GC. Rank by RAM impact in modded blockstate-heavy packs.

## Feature modules (complete set — only ~6 options)

Upstream config (`FerriteConfig`):

| Option | Default | Role |
| --- | --- | --- |
| `replaceNeighborLookup` (`NEIGHBOR_LOOKUP`) | on | FastMap neighbor table |
| `replacePropertyMap` (`PROPERTY_MAP`) | on | Derive properties from FastMap (needs neighbor) |
| `blockstateCacheDeduplication` | on | Dedupe `BlockStateBase.Cache` collision/shapes |
| `dataComponentPatch` | on | Shrink empty `PatchedDataComponentMap` |
| `compactFastMap` | **off** | Smaller/slower FastMap keys |
| `useSmallThreadingDetector` | **off** | Tiny thread detector on `PalettedContainer` |

## Impact ranking

| # | Feature | Mixin / impl classes | Mojmap 26.3 class + method | Impact |
| --- | --- | --- | --- | --- |
| 1 | Neighbor lookup / FastMap | `StateDefinitionMixin`, `FastMapStateHolderMixin` + `fastmap/*` | `StateDefinition#createMultiPropertyStates`, `#createSinglePropertyStates`; `StateHolder` neighbor/populated map Overwrite | **S** RAM (all blockstates) |
| 2 | Property map replace | same FastMap holder | `StateHolder` property access paths | S RAM (depends on #1) |
| 3 | Blockstate cache dedup | `BlockStateBaseMixin`, `BlockStateCacheMixin` + `BlockStateCacheImpl`, shape hashers | `BlockBehaviour.BlockStateBase#initCache`; target `BlockBehaviour$BlockStateBase$Cache` fields `collisionShape`, `faceSturdy` | **S** RAM (shapes) |
| 4 | VoxelShape accessor layer | `mixin/accessors/*Access` | `VoxelShape`, `ArrayVoxelShape`, `SliceShape`, `DiscreteVoxelShape`, `BitSetDiscreteVoxelShape`, `SubShape`, `StateHolder` | support for #3 |
| 5 | Data component patch | `PatchedDataComponentMapMixin` | `net.minecraft.core.component.PatchedDataComponentMap` (empty/shared patch maps; `get`/`set`/`applyPatch`) | **A** RAM (items) |
| 6 | Compact FastMap | FastMap key impl switch | same as #1 | B (opt-in) |
| 7 | Small threading detector | `PalettedContainerMixin` | `PalettedContainer` thread-check fields (Overwrite) | B opt-in; crash risk |

Supporting non-mixin (must rewrite): `FastMap`, `FastMapKey`, `CompactFastMapKey`, `BinaryFastMapKey`, `VoxelShapeHash` / `ArrayVoxelShapeHash` / `SliceShapeHash` / `DiscreteVSHash`, `BlockStateCacheImpl`, `FastMapStateHolderImpl`, `SmallThreadingDetector`.

## What already exists in vulkan-perf

| Existing | Relation to FerriteCore |
| --- | --- |
| `mixin/memory/ShapesJoinCacheMixin` → `Shapes#join` | **Different idea**: interns join *results*, does not dedupe blockstate `Cache` or FastMap neighbors |
| `PerfConfig.MemoryConfig.internShapes` | Partial “memory” module only |
| README | “FerriteCore (partial) — VoxelShape join interning” |

**Missing:** FastMap neighbor tables, property map elimination, blockstate cache dedup, data component sharing, threading detector, shape hash infrastructure.

## Concrete files to create (`dev.vulkanperf.*`)

### Config

```
src/main/java/dev/vulkanperf/config/PerfConfig.java
  MemoryConfig:
    internShapes (existing)
    replaceNeighborLookup
    replacePropertyMap
    blockstateCacheDedup
    dataComponentPatch
    compactFastMap (opt-in)
    smallThreadingDetector (opt-in)
```

### Core rewrite (no paste)

```
src/main/java/dev/vulkanperf/memory/fastmap/FastMap.java
src/main/java/dev/vulkanperf/memory/fastmap/FastMapKey.java
src/main/java/dev/vulkanperf/memory/fastmap/CompactFastMapKey.java
src/main/java/dev/vulkanperf/memory/fastmap/BinaryFastMapKey.java
src/main/java/dev/vulkanperf/memory/fastmap/FastMapStateHolder.java          # duck/interface
src/main/java/dev/vulkanperf/memory/cache/BlockStateCacheDeduper.java
src/main/java/dev/vulkanperf/memory/cache/VoxelShapeInterner.java
src/main/java/dev/vulkanperf/memory/hash/VoxelShapeEquivalence.java
src/main/java/dev/vulkanperf/memory/hash/ArrayShapeEquivalence.java
src/main/java/dev/vulkanperf/memory/hash/SliceShapeEquivalence.java
src/main/java/dev/vulkanperf/memory/components/EmptyPatchSharing.java
src/main/java/dev/vulkanperf/memory/thread/SmallThreadDetect.java
```

### Mixins / accessors

```
src/main/java/dev/vulkanperf/mixin/memory/fastmap/StateDefinitionMixin.java
  → StateDefinition#createMultiPropertyStates / #createSinglePropertyStates
src/main/java/dev/vulkanperf/mixin/memory/fastmap/StateHolderFastMapMixin.java
  → StateHolder (neighbor table / getNeighbor)
src/main/java/dev/vulkanperf/mixin/memory/blockstate/BlockStateBaseInitCacheMixin.java
  → BlockBehaviour.BlockStateBase#initCache
src/main/java/dev/vulkanperf/mixin/memory/blockstate/BlockStateCacheAccessMixin.java
  → targets="…BlockBehaviour$BlockStateBase$Cache"
src/main/java/dev/vulkanperf/mixin/memory/components/PatchedDataComponentMapMixin.java
  → PatchedDataComponentMap
src/main/java/dev/vulkanperf/mixin/memory/thread/PalettedContainerThreadMixin.java
  → PalettedContainer (opt-in)
src/main/java/dev/vulkanperf/mixin/memory/accessors/VoxelShapeAccess.java
src/main/java/dev/vulkanperf/mixin/memory/accessors/ArrayVoxelShapeAccess.java
src/main/java/dev/vulkanperf/mixin/memory/accessors/SliceShapeAccess.java
src/main/java/dev/vulkanperf/mixin/memory/accessors/DiscreteVoxelShapeAccess.java
src/main/java/dev/vulkanperf/mixin/memory/accessors/BitSetDiscreteVoxelShapeAccess.java
src/main/java/dev/vulkanperf/mixin/memory/accessors/SubShapeAccess.java
src/main/java/dev/vulkanperf/mixin/memory/accessors/StateHolderAccess.java
```

Keep existing:

```
src/main/java/dev/vulkanperf/mixin/memory/ShapesJoinCacheMixin.java
```

Register new mixin JSON entries (or split `vulkanperf.memory.mixins.json` like Ferrite).

## Compile notes (Mojmap 26.3)

- `BlockBehaviour.BlockStateBase#initCache` — confirmed in sources.
- Cache class: `net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase$Cache`.
- `StateDefinition` / `StateHolder` / `PatchedDataComponentMap` / `PalettedContainer` — present.
- Shape class names may be `ArrayVoxelShape` etc. under `net.minecraft.world.phys.shapes` — confirm with accessors before writing hashes.
- Ferrite `compatibilityLevel` is JAVA_17 in JSON; vulkan-perf uses **JAVA_25** — fine.

## Port strategy

1. Blockstate cache dedup + shape accessors (complements existing join interning).
2. FastMap neighbor + property map (largest modded RAM win).
3. Data component empty-map sharing.
4. Opt-in compact map + threading detector behind flags; default off.
5. README: upgrade “FerriteCore (partial)” → full when options 1–4 land.
EOF