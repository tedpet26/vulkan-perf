package dev.vulkanperf.mixin.worldgen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.levelgen.LegacyRandomSource;

@Mixin(LegacyRandomSource.LegacyPositionalRandomFactory.class)
public interface LegacyPositionalRandomSeedAccessor {
	@Accessor("seed")
	long vp$seed();
}
