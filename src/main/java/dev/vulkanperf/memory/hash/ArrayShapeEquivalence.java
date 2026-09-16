package dev.vulkanperf.memory.hash;

import dev.vulkanperf.mixin.memory.accessors.ArrayVoxelShapeAccessor;
import it.unimi.dsi.fastutil.Hash;

import java.util.Objects;

/**
 * Structural hash/equality for {@code ArrayVoxelShape}, comparing the point
 * coordinate lists and underlying discrete shape rather than object identity.
 */
public final class ArrayShapeEquivalence implements Hash.Strategy<ArrayVoxelShapeAccessor> {
	public static final ArrayShapeEquivalence INSTANCE = new ArrayShapeEquivalence();

	private ArrayShapeEquivalence() {
	}

	@Override
	public int hashCode(ArrayVoxelShapeAccessor shape) {
		if (shape == null) {
			return 0;
		}
		int result = Objects.hash(shape.vulkanperf$getXPoints(), shape.vulkanperf$getYPoints(), shape.vulkanperf$getZPoints());
		return 31 * result + DiscreteShapeEquivalence.INSTANCE.hashCode(shape.vulkanperf$getShape());
	}

	@Override
	public boolean equals(ArrayVoxelShapeAccessor a, ArrayVoxelShapeAccessor b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		return Objects.equals(a.vulkanperf$getXPoints(), b.vulkanperf$getXPoints())
				&& Objects.equals(a.vulkanperf$getYPoints(), b.vulkanperf$getYPoints())
				&& Objects.equals(a.vulkanperf$getZPoints(), b.vulkanperf$getZPoints())
				&& DiscreteShapeEquivalence.INSTANCE.equals(a.vulkanperf$getShape(), b.vulkanperf$getShape());
	}
}
