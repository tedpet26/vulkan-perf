package dev.vulkanperf.mixin.logic.shapes;

import dev.vulkanperf.config.PerfConfig;
import dev.vulkanperf.logic.shapes.JoinIsNotEmptyCache;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Shapes.class)
public abstract class ShapesJoinIsNotEmptyMixin {
	@Inject(method = "joinIsNotEmpty", at = @At("HEAD"), cancellable = true)
	private static void vulkanperf$cachedJoinIsNotEmpty(VoxelShape first, VoxelShape second, BooleanOp op, CallbackInfoReturnable<Boolean> cir) {
		if (!PerfConfig.get().logic.joinIsNotEmptyCache || first == null || second == null || op == null) {
			return;
		}
		Boolean cached = JoinIsNotEmptyCache.get(first, second, op);
		if (cached != null) {
			cir.setReturnValue(cached);
		}
	}

	@Inject(method = "joinIsNotEmpty", at = @At("RETURN"))
	private static void vulkanperf$storeJoinIsNotEmpty(VoxelShape first, VoxelShape second, BooleanOp op, CallbackInfoReturnable<Boolean> cir) {
		if (!PerfConfig.get().logic.joinIsNotEmptyCache || first == null || second == null || op == null) {
			return;
		}
		JoinIsNotEmptyCache.put(first, second, op, cir.getReturnValue());
	}
}
