package dev.vulkanperf.mixin.logic.pathing;

import dev.vulkanperf.config.PerfConfig;
import dev.vulkanperf.logic.pathing.PathTypeCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WalkNodeEvaluator.class)
public abstract class WalkNodeEvaluatorPathTypeMixin {
	@Inject(method = "getPathTypeFromState", at = @At("HEAD"), cancellable = true)
	private static void vulkanperf$cachedPathType(BlockGetter level, BlockPos pos, CallbackInfoReturnable<PathType> cir) {
		if (!PerfConfig.get().logic.pathTypeCache) {
			return;
		}
		BlockState state = level.getBlockState(pos);
		PathType cached = PathTypeCache.get(state);
		if (cached != null) {
			cir.setReturnValue(cached);
		}
	}

	@Inject(method = "getPathTypeFromState", at = @At("RETURN"))
	private static void vulkanperf$storePathType(BlockGetter level, BlockPos pos, CallbackInfoReturnable<PathType> cir) {
		if (!PerfConfig.get().logic.pathTypeCache) {
			return;
		}
		PathType computed = cir.getReturnValue();
		if (computed != null) {
			PathTypeCache.put(level.getBlockState(pos), computed);
		}
	}
}
