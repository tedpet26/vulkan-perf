package dev.vulkanperf.mixin.memory;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.ConcurrentHashMap;

/**
 * FerriteCore-inspired VoxelShape join interning: identical join results are reused.
 */
@Mixin(Shapes.class)
public abstract class ShapesJoinCacheMixin {
	@Unique
	private static final ConcurrentHashMap<JoinKey, VoxelShape> VULKANPERF$JOIN = new ConcurrentHashMap<>();

	@Inject(method = "join", at = @At("HEAD"), cancellable = true)
	private static void vulkanperf$reuseJoin(VoxelShape first, VoxelShape second, BooleanOp op, CallbackInfoReturnable<VoxelShape> cir) {
		if (!PerfConfig.get().memory.enabled || !PerfConfig.get().memory.internShapes || first == null || second == null || op == null) {
			return;
		}
		VoxelShape hit = VULKANPERF$JOIN.get(new JoinKey(first, second, op));
		if (hit != null) {
			cir.setReturnValue(hit);
		}
	}

	@Inject(method = "join", at = @At("RETURN"))
	private static void vulkanperf$storeJoin(VoxelShape first, VoxelShape second, BooleanOp op, CallbackInfoReturnable<VoxelShape> cir) {
		if (!PerfConfig.get().memory.enabled || !PerfConfig.get().memory.internShapes || first == null || second == null || op == null) {
			return;
		}
		if (VULKANPERF$JOIN.size() > 1024) {
			VULKANPERF$JOIN.clear();
		}
		VoxelShape result = cir.getReturnValue();
		if (result != null) {
			VULKANPERF$JOIN.put(new JoinKey(first, second, op), result);
		}
	}

	@Unique
	private record JoinKey(VoxelShape a, VoxelShape b, BooleanOp op) {
	}
}
