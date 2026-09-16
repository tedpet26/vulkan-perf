# vulkan-perf

Fabric performance suite for Minecraft 26.3, built against Blaze3D so it runs
on Vulkan and OpenGL.

Requires **Fabric Loader**, **Fabric API**, and **Sodium** (for the extras
pages). Do not install Lithium, C2ME, Sodium Extra, or the other replaced mods
alongside this jar.

Config: `config/vulkanperf.json`  
Command: `/vulkanperf` (modules, reload)

Graphics API: Video Settings → Graphics API. Use the Vulkan option when the
driver supports it; OpenGL remains the fallback.
