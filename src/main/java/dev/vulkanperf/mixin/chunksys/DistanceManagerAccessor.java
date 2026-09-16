package dev.vulkanperf.mixin.chunksys;

import org.spongepowered.asm.mixin.Mixin;

/**
 * Accessor placeholder for DistanceManager ticket state. No fields are
 * currently needed by active features; kept so downstream mixins can add
 * @Shadow/@Accessor members without changing registration.
 */
@Mixin(net.minecraft.server.level.DistanceManager.class)
public abstract class DistanceManagerAccessor {
}
