# Full-port inventories (MC 26.3 Fabric / Mojmap)

Research clones live under `/workspace/ref/github/` (gitignored). Mojmap 26.3
sources were extracted to `/tmp/mc-check` from loom-cache jars.

**License policy for all ports: clean-room rewrite only — no paste from upstream.**

| Mod | Recommended ref | License | vulkan-perf today |
| --- | --- | --- | --- |
| Lithium | tag `mc26.3-0.26.0` | LGPL-3.0 | `logic` partial (~8 mixins) |
| ImmediatelyFast | branch `26.2` (no 26.3 yet) | LGPL-3.0 | **none** (listed “not replaced”) |
| FerriteCore | branch `26.1` / Modrinth `9.0.0-fabric` (26.1–26.2) | MIT | `memory` full (FastMap, cache dedup, empty patches, join intern; compact FastMap + thread detector opt-in) |
| C2ME | branch `dev/26.3.0` | MIT (+ some ARR dirs) | `chunks` thread-pool redirect only |

See per-mod files in this directory.
