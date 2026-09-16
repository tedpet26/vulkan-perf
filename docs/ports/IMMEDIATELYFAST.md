# ImmediatelyFast (RaphiMC) — full port inventory for vulkan-perf

## Recommended ref

| Item | Value |
| --- | --- |
| **Recommended for study** | Git branch **`26.2`** (HEAD; Modrinth latest Fabric `1.16.4+26.2-fabric`) |
| **26.3 status** | **No tag/branch/Modrinth build for 26.3** as of 2026-09-16 |
| Clone | `/workspace/ref/github/immediatelyfast` @ `26.2` |
| Upstream package | `net.raphimc.immediatelyfast.injection.mixins.*` |
| Mixins | **23** client entries in `immediatelyfast-common.mixins.json` |
| License | **LGPL-3.0** → rewrite only, no paste |

**Critical 26.3 retarget:** IF 26.2 uses `com.mojang.blaze3d.opengl.*`. Mojmap **26.3** moved these to **`com.mojang.renderpearl.backend.opengl.*`** (`GlCommandEncoder`, `GlSurface`, `GlDebug`, `GlStateManager`, `GlConst`). Any port must re-verify method bodies against `/tmp/mc-check` / loom sources — do not assume 26.2 mixin targets compile.

vulkan-perf already runs on Blaze3D/RenderPearl (Vulkan+GL). IF’s GL-only framebuffer/buffer tricks need a **backend-aware** design (skip or alternate path on Vulkan).

## Impact ranking (client FPS)

Upstream is small; rank by FPS in entity/GUI/map-heavy scenes.

| # | Feature module | Mixin class(es) | Mojmap 26.3 class + method | Est. impact |
| --- | --- | --- | --- | --- |
| 1 | `enhanced_batching` | `MixinRenderTypeFeatureRenderer_Group` | `net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer$Group.<init>(…, canReorder, …)` — force reorder | **S** (entity/BE/particle batching) |
| 2 | `enhanced_batching` | `MixinScissorState` | `com.mojang.blaze3d.systems.ScissorState` | S (batching helper) |
| 3 | `map_atlas_generation` | `MixinMapRenderer` | `MapRenderer#render`, `#extractRenderState` | **S** (many maps / item frames) |
| 4 | `map_atlas_generation` | `MixinMapTextureManager`, `…_MapInstance` | `MapTextureManager#getOrCreateMapInstance`, `#resetData`; nested `MapTextureManager$MapInstance#updateTextureIfNeeded` | S |
| 5 | `map_atlas_generation` | `MixinGuiGraphicsExtractor`, `MixinMapRenderState` | `GuiGraphicsExtractor#map`; `MapRenderState` fields | A |
| 6 | `font_atlas_resizing` | `MixinFontTexture` | `net.minecraft.client.gui.font.FontTexture.<init>` (+ atlas size) | **A–S** (text-heavy HUD) |
| 7 | `fast_text_lookup` | `MixinTextFeatureRenderer_GlyphRenderer`, `MixinNameTagFeatureRenderer_GlyphRenderer` | Nested glyph renderers under feature renderers (`acceptRenderable`) — resolve exact nested names in 26.3 sources | A |
| 8 | `skip_text_translucency_sorting` | `MixinRenderTypes` | `net.minecraft.client.renderer.rendertype.RenderTypes` (translucent text layer flags) | A |
| 9 | `batch_animated_item_updates` | `MixinGuiRenderer` | `GuiRenderer#prepareItemElements`, `#endFrame`, `#invalidateItemAtlas`, `#close` | A (hotbar/GUI items) |
| 10 | `avoid_redundant_framebuffer_switching` | `MixinGlCommandEncoder`, `MixinGlSurface` | **`com.mojang.renderpearl.backend.opengl.GlCommandEncoder#submitRenderPass`, `#presentTexture`**; `GlSurface#present` | A on GL; **N/A or rewrite for Vulkan** |
| 11 | `fix_slow_buffer_upload_on_apple_gpu` | `MixinGlCommandEncoder` | `GlCommandEncoder#writeToBuffer` | A on Apple GL only |
| 12 | `sign_text_buffering` (experimental) | `MixinAbstractSignRenderer`, `MixinSignText` | `AbstractSignRenderer#submitSignText`; `SignText#getRenderMessages` | B–A (sign forests) |
| 13 | `core` | `MixinMinecraft`, `MixinDebugScreenEntries` | `Minecraft.<init>`, `#setLevel`; `DebugScreenEntries.<clinit>` | support |
| 14 | `resource_pack_conflict_handling` | `MixinShaderManager` | `ShaderManager#apply(Configs, ResourceManager, ProfilerFiller)` | compat, not FPS |
| 15 | Iris compat under `enhanced_batching.compat.iris` | optional | only if Iris present | conditional |
| 16 | `print_additional_error_information` | `MixinGlDebug` | `GlDebug#enableDebugCallback`, `#printDebugLog` | debug only |

Also port non-mixin feature code (as **original** implementations):

- Map atlas allocator / texture packing
- Sign text atlas (experimental)
- Runtime config + GPU vendor heuristics (`ImmediatelyFastConfig` flags)

## What already exists in vulkan-perf

| Area | Status |
| --- | --- |
| ImmediatelyFast | **Not implemented** — README “Not replaced” |
| Related | `extras` toggles (particles, nametags, item frames) reduce draw *count*, not batching |
| Related | `particles` frustum cull; `culling` entity occlusion |
| Related | `hudspread` debug overlay spread — not IF HUD batching |
| Backend | `client/backend/BackendGuard` + `MinecraftBackendMixin` — hook for GL-vs-Vulkan gating |

**No** map atlas, font atlas resize, render-type reorder, sign buffering, or GL command-encoder mixins.

## Concrete files to create (`dev.vulkanperf.*`)

### Config / entry

```
src/main/java/dev/vulkanperf/config/PerfConfig.java          # add ImmediatelyFastConfig / clientrender flags
src/client/java/dev/vulkanperf/client/imfast/ImFastRuntime.java
src/client/java/dev/vulkanperf/client/imfast/GpuHeuristics.java
src/client/resources/vulkanperf.client.mixins.json            # register
```

### Feature support

```
src/client/java/dev/vulkanperf/client/imfast/batching/ReorderGate.java
src/client/java/dev/vulkanperf/client/imfast/map/MapAtlasAllocator.java
src/client/java/dev/vulkanperf/client/imfast/map/MapAtlasTexture.java
src/client/java/dev/vulkanperf/client/imfast/font/FontAtlasSizing.java
src/client/java/dev/vulkanperf/client/imfast/sign/SignTextAtlas.java
src/client/java/dev/vulkanperf/client/imfast/gui/AnimatedItemBatcher.java
```

### Mixins (26.3 Mojmap targets)

```
src/client/java/dev/vulkanperf/client/mixin/imfast/RenderTypeGroupReorderMixin.java
  → RenderTypeFeatureRenderer$Group.<init>
src/client/java/dev/vulkanperf/client/mixin/imfast/ScissorStateMixin.java
  → ScissorState
src/client/java/dev/vulkanperf/client/mixin/imfast/MapRendererMixin.java
  → MapRenderer#render / #extractRenderState
src/client/java/dev/vulkanperf/client/mixin/imfast/MapTextureManagerMixin.java
  → MapTextureManager
src/client/java/dev/vulkanperf/client/mixin/imfast/MapInstanceMixin.java
  → MapTextureManager$MapInstance
src/client/java/dev/vulkanperf/client/mixin/imfast/GuiGraphicsExtractorMapMixin.java
  → GuiGraphicsExtractor#map
src/client/java/dev/vulkanperf/client/mixin/imfast/MapRenderStateMixin.java
  → MapRenderState
src/client/java/dev/vulkanperf/client/mixin/imfast/FontTextureMixin.java
  → FontTexture
src/client/java/dev/vulkanperf/client/mixin/imfast/TextGlyphLookupMixin.java
  → (resolve GlyphRenderer nested class in 26.3)
src/client/java/dev/vulkanperf/client/mixin/imfast/NameTagGlyphLookupMixin.java
src/client/java/dev/vulkanperf/client/mixin/imfast/RenderTypesTextSortMixin.java
  → RenderTypes
src/client/java/dev/vulkanperf/client/mixin/imfast/GuiRendererItemBatchMixin.java
  → GuiRenderer#prepareItemElements / #endFrame
src/client/java/dev/vulkanperf/client/mixin/imfast/GlCommandEncoderFramebufferMixin.java
  → com.mojang.renderpearl.backend.opengl.GlCommandEncoder (GL only)
src/client/java/dev/vulkanperf/client/mixin/imfast/GlSurfacePresentMixin.java
  → GlSurface#present
src/client/java/dev/vulkanperf/client/mixin/imfast/GlCommandEncoderAppleUploadMixin.java
  → GlCommandEncoder#writeToBuffer
src/client/java/dev/vulkanperf/client/mixin/imfast/AbstractSignRendererMixin.java
  → AbstractSignRenderer#submitSignText
src/client/java/dev/vulkanperf/client/mixin/imfast/SignTextMixin.java
  → SignText#getRenderMessages
src/client/java/dev/vulkanperf/client/mixin/imfast/MinecraftImFastInitMixin.java
  → Minecraft.<init> / #setLevel
```

## Compile / design risks

1. **Package rename** `blaze3d.opengl` → `renderpearl.backend.opengl` (26.3).
2. **Vulkan path:** framebuffer bind elision and Apple upload fix are GL-specific; gate with `BackendGuard`.
3. **Enhanced batching** changes draw order — must validate with Sodium / translucency.
4. Prefer waiting for upstream `26.3` branch if signatures thrash; otherwise port from `26.2` with Mojmap re-bind.

## Port strategy

1. Map atlas + font atlas + render-type reorder (biggest FPS, mostly API-stable).
2. GUI item batching + text lookup/sort skip.
3. GL command-encoder opts behind backend check.
4. Experimental sign buffering last.
5. Update README “Not replaced” → replaces ImmediatelyFast when feature-complete on both GL and Vulkan (or document GL-only subset).
EOF