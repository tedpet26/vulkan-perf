# vulkan-perf

Fabric performance suite for Minecraft 26.3, built against Blaze3D so it runs
on Vulkan and OpenGL.

Requires **Fabric Loader**, **Fabric API**, and **Sodium** (for the extras
pages).

Config: `config/vulkanperf.json`  
Command: `/vulkanperf` (modules, reload)

Graphics API: Video Settings → Graphics API. Use the Vulkan option when the
driver supports it; OpenGL remains the fallback.

## Implemented (breaks these mods)

| Module | Replaces | Notes |
| --- | --- | --- |
| `extras` | Sodium Extra | Visual/detail toggles + Sodium config pages |
| `logic` | Lithium (full common slice) | Class-group collision queries + per-section collision indexes, lazy entity-move collider materialisation, small-box section iteration, **sleeping block entities** (furnace, brewing stand, campfire, shulker box, hoppers with watcher-bus invalidation + cooldown compensation), hopper container cache + idle cooldown, joinIsNotEmpty cache, path + path-type caches, type-filtered entity maps, lambda-free raycasts, AI throttle, item merge, mob AI skip, empty-section random-tick skip |
| `culling` | Entity Culling | Async entity occlusion (multi-point raycasts, worker pool) + block-entity distance cull |
| `clientcache` | BadOptimizations (partial) | Toast skip + sky color cache |
| `power` | Dynamic FPS (partial) | Unfocused/hidden FPS cap + mute |
| `ping` | Fast IP Ping | Numeric-IP fast resolve |
| `hudspread` | Gnetum (partial) | Debug overlay work spread |
| `logging` | LogCleaner | Log4j spam filter |
| `packets` | PacketFixer | NBT/string/compression limit bumps |
| `reloadui` | RRLS | Skip later loading overlays |
| `memory` | FerriteCore | FastMap neighbor + property maps, blockstate cache dedup, empty component-patch sharing, VoxelShape join intern; opt-in compact FastMap and small threading detector (off by default) |
| `imfast` | ImmediatelyFast | Map/font atlases, text batching, GUI animated-item atlas, sign buffering; GL-only framebuffer skip + Apple upload (no-op on Vulkan) |
| `chunksys` | C2ME (contained slice) | Mid-tick chunk task drain, region-file cache config, region fsync relax, render-distance uncap; not the full chunk-system rewrite |
| `worldgen` | C2ME (worldgen/allocs) | Thread-confined worldgen randoms without atomics, pooled ore-vein BitSets, Identifier toString cache, fastutil NBT copy maps, synchronized structure-check/template caches (incl. MC-271899) |
| `network` | Krypton (Java slice) | SWAR-style frame decode with NUL-flood skip + retained slices + quiet exceptions, table-driven VarInt sizing/encode, single-pass string encode, shared frame prepender, dead-channel query guard, smaller Netty arenas. Native compression/cipher stay upstream-only; auto-disabled when Krypton is installed |
| `mfix` | ModernFix (slice) | One-time zip pack indexes for resource reloads, expiring OpenAL sound buffer cache, soft-referenced structure template cache. Auto-disabled when ModernFix is installed |
| `particles` | Particle Core (culling slice) | Bounding-box frustum cull, render-distance particle cull, per-tick lightmap cache. Auto-disabled when Particle Core / AsyncParticles is installed |
| `moreculling` | MoreCulling (slice) | Sign text back-face culling (leaves face culling is vanilla in 26.3). Auto-disabled when MoreCulling is installed |

## Not fully replaced

- `chunksys` covers C2ME's **scheduling/IO options** but not its chunk-system
  rewrite, DFC density-function compiler, threaded lighting, or natives/OpenCL
  (`rewriteChunkSystem` and `nativesMath` stay placeholders, default off).
- `network` covers Krypton's pure-Java paths; its **native compression and
  cipher** (velocity-native) stay upstream-only.
- `worldgen` covers C2ME's alloc/threading-fix classes; the aquifer rewrite and
  End-biome cache proved unnecessary (the shape check is already cached and End
  biomes are y-dependent in 26.3).
- BBE-style block-entity terrain baking, Ixeris event threading (mostly
  upstreamed by vanilla 26.3) and borderless fullscreen (CWB) are not ported.
- Lazy recipe search trees and the encoder-cache leak are **already vanilla in
  26.3**; the shape-full-block query is cached by vanilla too.

These mods are **not** broken: if installed, the matching vulkan-perf mixins
auto-disable (Krypton, ModernFix, MoreCulling, Particle Core, AsyncParticles).
C2ME also stays fully compatible.

Do **not** install these next to this jar (replaced by the modules above):
Lithium, Sodium Extra, Entity Culling, FerriteCore, ImmediatelyFast,
BadOptimizations, Dynamic FPS, Fast IP Ping, Gnetum, LogCleaner, PacketFixer,
RRLS.

See [docs/PERF_BENCH.md](docs/PERF_BENCH.md) for build validation and
comparison notes, and [docs/ports/](docs/ports/) for the port checklists.
