package dev.vulkanperf.mixin.logic.raycast;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.gen.Invoker;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The vanilla {@code clip} body allocates a capturing lambda per call (the block tracer captures
 * {@code this}); line-of-sight checks, explosions and projectile logic call it constantly. A
 * reusable per-thread tracer pair removes that allocation while keeping the exact traversal
 * semantics. Recursion (a shape getter calling clip again on the same thread) falls back to the
 * vanilla allocating path.
 */
@Mixin(BlockGetter.class)
public interface BlockGetterRaycastMixin {

	@Invoker("traverseBlocks")
	static <T, C> T vp$traverseBlocks(
		Vec3 from, Vec3 to, C context, BiFunction<C, BlockPos, T> consumer, Function<C, T> missFactory
	) {
		throw new AssertionError();
	}

	@Overwrite
	default BlockHitResult clip(final ClipContext c) {
		BlockGetter self = (BlockGetter) this;
		if (!PerfConfig.get().logic.fastRaycast) {
			return vp$vanillaClip(self, c);
		}
		VpBlockTracer tracer = VpBlockTracer.INSTANCE.get();
		if (tracer.busy) {
			return vp$vanillaClip(self, c);
		}
		tracer.busy = true;
		tracer.getter = self;
		try {
			return vp$traverseBlocks(c.getFrom(), c.getTo(), c, tracer, VpMissTracer.INSTANCE);
		} finally {
			tracer.busy = false;
			tracer.getter = null;
		}
	}

	default BlockHitResult vp$vanillaClip(BlockGetter self, ClipContext c) {
		return vp$traverseBlocks(
			c.getFrom(), c.getTo(), c,
			(context, pos) -> {
				BlockState blockState = self.getBlockState(pos);
				FluidState fluidState = self.getFluidState(pos);
				Vec3 from = context.getFrom();
				Vec3 to = context.getTo();
				VoxelShape blockShape = context.getBlockShape(blockState, self, pos);
				BlockHitResult blockResult = self.clipWithInteractionOverride(from, to, pos, blockShape, blockState);
				VoxelShape fluidShape = context.getFluidShape(fluidState, self, pos);
				BlockHitResult liquidResult = fluidShape.clip(from, to, pos);
				double blockDistanceSquared = blockResult == null ? Double.MAX_VALUE : context.getFrom().distanceToSqr(blockResult.getLocation());
				double liquidDistanceSquared = liquidResult == null ? Double.MAX_VALUE : context.getFrom().distanceToSqr(liquidResult.getLocation());
				return blockDistanceSquared <= liquidDistanceSquared ? blockResult : liquidResult;
			},
			VpMissTracer.INSTANCE
		);
	}

	final class VpBlockTracer implements BiFunction<ClipContext, BlockPos, BlockHitResult> {
		static final ThreadLocal<VpBlockTracer> INSTANCE = ThreadLocal.withInitial(VpBlockTracer::new);

		boolean busy;
		BlockGetter getter;

		@Override
		public BlockHitResult apply(ClipContext context, BlockPos pos) {
			BlockGetter self = this.getter;
			BlockState blockState = self.getBlockState(pos);
			FluidState fluidState = self.getFluidState(pos);
			Vec3 from = context.getFrom();
			Vec3 to = context.getTo();
			VoxelShape blockShape = context.getBlockShape(blockState, self, pos);
			BlockHitResult blockResult = self.clipWithInteractionOverride(from, to, pos, blockShape, blockState);
			VoxelShape fluidShape = context.getFluidShape(fluidState, self, pos);
			BlockHitResult liquidResult = fluidShape.clip(from, to, pos);
			double blockDistanceSquared = blockResult == null ? Double.MAX_VALUE : context.getFrom().distanceToSqr(blockResult.getLocation());
			double liquidDistanceSquared = liquidResult == null ? Double.MAX_VALUE : context.getFrom().distanceToSqr(liquidResult.getLocation());
			return blockDistanceSquared <= liquidDistanceSquared ? blockResult : liquidResult;
		}
	}

	final class VpMissTracer implements Function<ClipContext, BlockHitResult> {
		static final VpMissTracer INSTANCE = new VpMissTracer();

		@Override
		public BlockHitResult apply(ClipContext context) {
			Vec3 delta = context.getFrom().subtract(context.getTo());
			return BlockHitResult.miss(context.getTo(), Direction.getApproximateNearest(delta.x, delta.y, delta.z), BlockPos.containing(context.getTo()));
		}
	}
}
