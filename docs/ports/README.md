# Full-port inventories (MC 26.3 Fabric / Mojmap)

Research clones live under `/workspace/ref/github/` (gitignored). Mojmap 26.3
sources were extracted to `/tmp/mc-check` from loom-cache jars.

**License policy for all ports: clean-room rewrite only — no paste from upstream.**

| Mod | Recommended ref | License | vulkan-perf today |
| --- | --- | --- | --- |
| Lithium | tag `mc26.3-0.26.0` | LGPL-3.0 | `logic` broad common slice: class-group collision queries, lazy move sweep, small-box retrieval, **sleeping block entities** (furnace/brewing/campfire/shulker/hopper w/ watcher bus), raycast, type-filtered maps |
| ImmediatelyFast | branch `26.2` (no 26.3 yet) | LGPL-3.0 | **`imfast`** (26.3 rewrite; GL-only subset gated) |
| FerriteCore | branch `26.1` / Modrinth `9.0.0-fabric` (26.1–26.2) | MIT | `memory` full (FastMap, cache dedup, empty patches, join intern; compact FastMap + thread detector opt-in) |
| C2ME | branch `dev/26.3.0` | MIT (+ some ARR dirs) | `chunksys` scheduling + `worldgen` alloc/threading-fix slice; chunk-system rewrite/DFC/natives stay upstream |
| Krypton | tag `v0.3.1` (26.2) | LGPL-3.0 | `network` Java slice (frame decode, VarInt/string encode, shared prepender); natives upstream-only; defers to installed Krypton |
| ModernFix | fork branch `fabric/26.2` | LGPL-3.0 | `mfix` slice (zip pack index, sound buffer expiry, soft template cache); defers to installed ModernFix |
| Particle Core | master (26.2) | MIT | `particles` culling slice (bbox frustum, render distance, light cache); defers when installed |
| MoreCulling | tag `v1.9.0-beta.1` (26.3) | GPL-3.0 | `moreculling` sign back-face cull (leaves core already vanilla in 26.3); defers when installed |
| Debugify | main (26.2) | LGPL-3.0 | MC-271899 structure-template thread safety landed in `worldgen` |

See per-mod files in this directory.
