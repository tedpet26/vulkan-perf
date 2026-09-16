package dev.vulkanperf.memory.hash;

import dev.vulkanperf.mixin.memory.accessors.SliceShapeAccessor;
import it.unimi.dsi.fastutil.Hash;

import java.util.Objects;

/**
 * Structural hash/equality for {@code SliceShape}, comparing axis, the
 * derived discrete shape, and (recursively) the delegate shape it slices.
 */
public final class SliceShapeEquivalence implements Hash.Strategy<SliceShapeAccessor> {
	public static final SliceShapeEquivalence INSTANCE = new SliceShapeEquivalence();

	private SliceShapeEquivalence() {
	}

	@Override
	public int hashCode(SliceShapeAccessor shape) {
		if (shape == null) {
			return 0;
		}
		int result = Objects.hashCode(shape.vulkanperf$getAxis());
		result = 31 * result + DiscreteShapeEquivalence.INSTANCE.hashCode(shape.vulkanperf$getShape());
		return 31 * result + ShapeEquivalence.INSTANCE.hashCode(shape.vulkanperf$getDelegate());
	}

	@Override
	public boolean equals(SliceShapeAccessor a, SliceShapeAccessor b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		return Objects.equals(a.vulkanperf$getAxis(), b.vulkanperf$getAxis())
				&& DiscreteShapeEquivalence.INSTANCE.equals(a.vulkanperf$getShape(), b.vulkanperf$getShape())
				&& ShapeEquivalence.INSTANCE.equals(a.vulkanperf$getDelegate(), b.vulkanperf$getDelegate());
	}
}
