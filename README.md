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
| `logic` | Lithium (partial) | Hopper cache, entity-query cache, AI throttle, shape/path caches, item merge, mob AI skip |
| `culling` | Entity Culling | Entity occlusion + block-entity distance cull |
| `clientcache` | BadOptimizations (partial) | Toast skip + sky color cache |
| `power` | Dynamic FPS (partial) | Unfocused/hidden FPS cap + mute |
| `ping` | Fast IP Ping | Numeric-IP fast resolve |
| `hudspread` | Gnetum (partial) | Debug overlay work spread |
| `logging` | LogCleaner | Log4j spam filter |
| `packets` | PacketFixer | NBT/string/compression limit bumps |
| `reloadui` | RRLS | Skip later loading overlays |
| `memory` | FerriteCore | FastMap neighbor + property maps, blockstate cache dedup, empty component-patch sharing, VoxelShape join intern; opt-in compact FastMap and small threading detector (off by default) |
| `imfast` | ImmediatelyFast | Map/font atlases, text batching, GUI animated-item atlas; GL-only framebuffer skip + Apple upload (no-op on Vulkan) |

## Not replaced (install upstream if needed)

Do **not** expect parity with: **C2ME**, **Krypton**,
**ModernFix**, **MoreCulling**, **AsyncParticles**, **Particle Core**,
**Better Block Entities**, **Ixeris**, **Cubes Without Borders**, or
**Debugify**. Those mods are allowed alongside this jar; vulkan-perf does not
ship equivalent depth for them. The `memory` module covers **FerriteCore** and
the `imfast` module covers **ImmediatelyFast** — do not install those next to
this jar.

`chunks` only redirects worldgen/IO onto local thread pools — it is **not** a
C2ME-class chunk rewrite, so C2ME is not broken.

See [docs/PERF_BENCH.md](docs/PERF_BENCH.md) for build validation and comparison notes.
