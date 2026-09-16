package dev.vulkanperf.mixin.chunksys;

import org.spongepowered.asm.mixin.Mixin;

/**
 * Placeholder lighting hook: 26.3's ThreadedLevelLightEngine already runs on
 * its own task dispatcher; deeper parallel lighting needs ScalableLux-class
 * rework (see docs/ports/C2ME.md item 5). Kept as a no-op target so config
 * gating has a stable mixin to toggle.
 */
@Mixin(net.minecraft.server.level.ChunkMap.class)
public abstract class ChunkMapLightingMixin {
}
