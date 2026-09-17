package dev.vulkanperf.mixin.worldgen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource.LegacyPositionalRandomFactory;

/**
 * Legacy positional randoms are handed out per worldgen thread and never shared across threads;
 * the {@link java.util.concurrent.atomic.AtomicLong} seed in {@code LegacyRandomSource} adds a
 * CAS to every {@code nextLong} for nothing. {@code SingleThreadedRandomSource} keeps identical
 * sequences with a plain field.
 */
@Mixin(LegacyPositionalRandomFactory.class)
public abstract class LegacyPositionalRandomFactoryMixin {

	/**
	 * @reason drop the AtomicLong on thread-confined worldgen randoms
	 * @author vulkan-perf
	 */
	@Overwrite
	public RandomSource at(final int x, final int y, final int z) {
		long positionalSeed = Mth.getSeed(x, y, z);
		return new SingleThreadedRandomSource(positionalSeed ^ ((LegacyPositionalRandomSeedAccessor) this).vp$seed());
	}

	/**
	 * @reason drop the AtomicLong on thread-confined worldgen randoms
	 * @author vulkan-perf
	 */
	@Overwrite
	public RandomSource fromHashOf(final String name) {
		int positionalSeed = name.hashCode();
		return new SingleThreadedRandomSource(positionalSeed ^ ((LegacyPositionalRandomSeedAccessor) this).vp$seed());
	}

	/**
	 * @reason drop the AtomicLong on thread-confined worldgen randoms
	 * @author vulkan-perf
	 */
	@Overwrite
	public RandomSource fromSeed(final long seed) {
		return new SingleThreadedRandomSource(seed);
	}
}
