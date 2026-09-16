package dev.vulkanperf.memory.hash;

import dev.vulkanperf.mixin.memory.accessors.ArrayVoxelShapeAccessor;
import dev.vulkanperf.mixin.memory.accessors.SliceShapeAccessor;
import dev.vulkanperf.mixin.memory.accessors.VoxelShapeAccessor;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.world.phys.shapes.CubeVoxelShape;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Dispatches to the appropriate structural equivalence for whichever
 * concrete {@link VoxelShape} subtype is given, so shapes that are built
 * differently but describe the same volume hash/compare equal. Used to
 * deduplicate the collision-shape cache attached to every blockstate.
 */
public final class ShapeEquivalence implements Hash.Strategy<VoxelShape> {
	public static final ShapeEquivalence INSTANCE = new ShapeEquivalence();

	private ShapeEquivalence() {
	}

	@Override
	public int hashCode(VoxelShape shape) {
		if (shape == null) {
			return 0;
		}
		if (shape instanceof SliceShapeAccessor slice) {
			return SliceShapeEquivalence.INSTANCE.hashCode(slice);
		} else if (shape instanceof ArrayVoxelShapeAccessor array) {
			return ArrayShapeEquivalence.INSTANCE.hashCode(array);
		} else if (shape instanceof CubeVoxelShape) {
			return DiscreteShapeEquivalence.INSTANCE.hashCode(((VoxelShapeAccessor) shape).vulkanperf$getShape());
		}
		return shape.hashCode();
	}

	@Override
	public boolean equals(VoxelShape a, VoxelShape b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null || a.getClass() != b.getClass()) {
			return false;
		}
		if (a instanceof SliceShapeAccessor sliceA) {
			return SliceShapeEquivalence.INSTANCE.equals(sliceA, (SliceShapeAccessor) b);
		} else if (a instanceof ArrayVoxelShapeAccessor arrayA) {
			return ArrayShapeEquivalence.INSTANCE.equals(arrayA, (ArrayVoxelShapeAccessor) b);
		} else if (a instanceof CubeVoxelShape) {
			return DiscreteShapeEquivalence.INSTANCE.equals(
					((VoxelShapeAccessor) a).vulkanperf$getShape(), ((VoxelShapeAccessor) b).vulkanperf$getShape()
			);
		}
		return a.equals(b);
	}
}
