package dev.vulkanperf.mixin.logic.poi;

import dev.vulkanperf.config.PerfConfig;
import dev.vulkanperf.logic.poi.PoiQueryCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Caches the two-argument {@code findClosest(predicate, center, radius, occupancy)}
 * overload per tick. Predicate identity is instance-based (lambdas allocated per
 * sensor run defeat the cache, repeat callers benefit).
 */
@Mixin(PoiManager.class)
public abstract class PoiManagerFindClosestMixin {
	@Inject(method = "findClosest(Ljava/util/function/Predicate;Lnet/minecraft/core/BlockPos;ILnet/minecraft/world/entity/ai/village/poi/PoiManager$Occupancy;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$cachedFindClosest(Object predicate, BlockPos center, int radius, Object occupancy, CallbackInfoReturnable<Optional<?>> cir) {
		if (!PerfConfig.get().logic.poiCache || predicate == null || center == null || !(predicate instanceof PoiQueryCache.PredicateBox box)) {
			return;
		}
		Object cached = PoiQueryCache.get(box, center, radius, occupancy);
		if (cached instanceof Optional<?> opt) {
			cir.setReturnValue(opt);
		}
	}

	@Inject(method = "findClosest(Ljava/util/function/Predicate;Lnet/minecraft/core/BlockPos;ILnet/minecraft/world/entity/ai/village/poi/PoiManager$Occupancy;)Ljava/util/Optional;", at = @At("RETURN"))
	private void vulkanperf$storeFindClosest(Object predicate, BlockPos center, int radius, Object occupancy, CallbackInfoReturnable<Optional<?>> cir) {
		if (!PerfConfig.get().logic.poiCache || predicate == null || center == null || !(predicate instanceof PoiQueryCache.PredicateBox box)) {
			return;
		}
		PoiQueryCache.put(box, center, radius, occupancy, cir.getReturnValue());
	}
}
