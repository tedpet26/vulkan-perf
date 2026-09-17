package dev.vulkanperf.mixin.worldgen;

import java.util.BitSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.level.levelgen.feature.OreFeature;

/**
 * Ore veins allocate a fresh {@code BitSet} (sizeXZ * sizeY * sizeXZ bits) per placement attempt;
 * a per-thread pool of exactly-sized, pre-cleared bitsets removes that churn during generation.
 */
@Mixin(OreFeature.class)
public abstract class OreFeatureBitSetPoolMixin {
	private static final Int2ObjectOpenHashMap<ThreadLocal<BitSet>> POOLS = new Int2ObjectOpenHashMap<>();

	@Redirect(
		method = "doPlace",
		at = @At(value = "NEW", target = "(I)Ljava/util/BitSet;")
	)
	private static BitSet vp$pooledBitSet(int bitCount) {
		ThreadLocal<BitSet> pool = POOLS.get(bitCount);
		if (pool == null) {
			BitSet fresh = new BitSet(bitCount);
			POOLS.put(bitCount, ThreadLocal.withInitial(() -> new BitSet(bitCount)));
			return fresh;
		}
		BitSet pooled = pool.get();
		pooled.clear();
		return pooled;
	}
}
