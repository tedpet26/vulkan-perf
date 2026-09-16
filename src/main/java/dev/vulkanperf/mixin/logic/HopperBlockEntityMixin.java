package dev.vulkanperf.mixin.logic;

import dev.vulkanperf.chunks.ChunkWorkers;
import dev.vulkanperf.config.PerfConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {
	private static final int VULKANPERF$MAX = 48;

	@Unique
	private static final ThreadLocal<HopperCache> VULKANPERF$CACHE = ThreadLocal.withInitial(HopperCache::new);

	@Inject(method = "getContainerAt(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/Container;", at = @At("HEAD"), cancellable = true)
	private static void vulkanperf$reuseContainer(Level level, BlockPos pos, CallbackInfoReturnable<Container> cir) {
		if (!PerfConfig.get().logic.hopper || ChunkWorkers.isChunkWorker() || pos == null) {
			return;
		}
		HopperCache cache = VULKANPERF$CACHE.get();
		if (cache.level != level || cache.tick != level.getGameTime()) {
			return;
		}
		Container hit = cache.byPos.get(pos.asLong());
		if (cache.byPos.containsKey(pos.asLong())) {
			cir.setReturnValue(hit);
		}
	}

	@Inject(method = "getContainerAt(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/Container;", at = @At("RETURN"))
	private static void vulkanperf$storeContainer(Level level, BlockPos pos, CallbackInfoReturnable<Container> cir) {
		if (!PerfConfig.get().logic.hopper || ChunkWorkers.isChunkWorker() || pos == null) {
			return;
		}
		HopperCache cache = VULKANPERF$CACHE.get();
		if (cache.level != level || cache.tick != level.getGameTime()) {
			cache.level = level;
			cache.tick = level.getGameTime();
			cache.byPos.clear();
		}
		cache.byPos.put(pos.asLong(), cir.getReturnValue());
		if (cache.byPos.size() > VULKANPERF$MAX) {
			cache.byPos.clear();
		}
	}

	@Unique
	private static final class HopperCache {
		private Level level;
		private long tick = Long.MIN_VALUE;
		private final LinkedHashMap<Long, Container> byPos = new LinkedHashMap<>(32, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<Long, Container> eldest) {
				return size() > VULKANPERF$MAX;
			}
		};
	}
}
