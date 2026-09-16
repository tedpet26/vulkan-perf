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
| `logic` | Lithium (Tier S/A) | Hopper container cache + idle-cooldown sleep, entity-query cache, AI throttle, shape join + joinIsNotEmpty caches, path + path-type caches, item merge, mob AI skip, empty-section random-tick skip |
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
| `chunksys` | C2ME (scheduling slice) | Mid-tick chunk task drain, region-file cache config, render-distance uncap; not the full chunk-system rewrite |

## Not fully replaced

`chunksys` covers C2ME's **scheduling/IO options** but not its chunk-system
rewrite, DFC bytecode compiler, threaded lighting, or natives/OpenCL — those
remain upstream-only (`rewriteChunkSystem` and `nativesMath` config flags are
placeholders, default off). C2ME is **not** broken, so the real mod can be
installed alongside for the full rewrite.

Do **not** install these next to this jar (replaced by the modules above):
Lithium, Sodium Extra, Entity Culling, FerriteCore, ImmediatelyFast,
BadOptimizations, Dynamic FPS, Fast IP Ping, Gnetum, LogCleaner, PacketFixer,
RRLS.

Allowed alongside: C2ME, Krypton, ModernFix, MoreCulling, AsyncParticles,
Particle Core, Better Block Entities, Ixeris, Cubes Without Borders, Debugify.

See [docs/PERF_BENCH.md](docs/PERF_BENCH.md) for build validation and
comparison notes, and [docs/ports/](docs/ports/) for the port checklists.
