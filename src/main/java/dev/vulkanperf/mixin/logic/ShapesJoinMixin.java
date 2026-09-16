package dev.vulkanperf.mixin.logic;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.ConcurrentHashMap;

@Mixin(Shapes.class)
public abstract class ShapesJoinMixin {
	@Unique
	private static final ConcurrentHashMap<ShapeKey, VoxelShape> VULKANPERF$OR_CACHE = new ConcurrentHashMap<>();

	@Inject(method = "or", at = @At("HEAD"), cancellable = true)
	private static void vulkanperf$reuseOr(VoxelShape a, VoxelShape b, CallbackInfoReturnable<VoxelShape> cir) {
		if (!PerfConfig.get().logic.voxelShapes || a == null || b == null) {
			return;
		}
		VoxelShape cached = VULKANPERF$OR_CACHE.get(new ShapeKey(a, b));
		if (cached != null) {
			cir.setReturnValue(cached);
		}
	}

	@Inject(method = "or", at = @At("RETURN"))
	private static void vulkanperf$storeOr(VoxelShape a, VoxelShape b, CallbackInfoReturnable<VoxelShape> cir) {
		if (!PerfConfig.get().logic.voxelShapes || a == null || b == null) {
			return;
		}
		if (VULKANPERF$OR_CACHE.size() > 512) {
			VULKANPERF$OR_CACHE.clear();
		}
		VoxelShape result = cir.getReturnValue();
		if (result != null) {
			VULKANPERF$OR_CACHE.put(new ShapeKey(a, b), result);
		}
	}

	@Unique
	private record ShapeKey(VoxelShape a, VoxelShape b) {
	}
}
