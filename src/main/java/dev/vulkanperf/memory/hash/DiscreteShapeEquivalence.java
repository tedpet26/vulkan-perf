package dev.vulkanperf.memory.hash;

import dev.vulkanperf.mixin.memory.accessors.BitSetDiscreteVoxelShapeAccessor;
import dev.vulkanperf.mixin.memory.accessors.DiscreteVoxelShapeAccessor;
import dev.vulkanperf.mixin.memory.accessors.SubShapeAccessor;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

import java.util.Objects;

/**
 * Structural hash/equality for {@link DiscreteVoxelShape}s, recursing through
 * {@code SubShape} parents and comparing {@code BitSetDiscreteVoxelShape}
 * storage directly, so two independently-built shapes covering the same
 * voxels are considered equal for deduplication purposes.
 */
public final class DiscreteShapeEquivalence implements Hash.Strategy<DiscreteVoxelShape> {
	public static final DiscreteShapeEquivalence INSTANCE = new DiscreteShapeEquivalence();

	private DiscreteShapeEquivalence() {
	}

	@Override
	public int hashCode(DiscreteVoxelShape shape) {
		if (shape == null) {
			return 0;
		}
		DiscreteVoxelShapeAccessor sizes = (DiscreteVoxelShapeAccessor) shape;
		int result = sizes.vulkanperf$getXSize();
		result = 31 * result + sizes.vulkanperf$getYSize();
		result = 31 * result + sizes.vulkanperf$getZSize();
		if (shape instanceof SubShapeAccessor sub) {
			result = 31 * result + sub.vulkanperf$getStartX();
			result = 31 * result + sub.vulkanperf$getStartY();
			result = 31 * result + sub.vulkanperf$getStartZ();
			result = 31 * result + sub.vulkanperf$getEndX();
			result = 31 * result + sub.vulkanperf$getEndY();
			result = 31 * result + sub.vulkanperf$getEndZ();
			return 31 * result + hashCode(sub.vulkanperf$getParent());
		} else if (shape instanceof BitSetDiscreteVoxelShapeAccessor bitSet) {
			result = 31 * result + bitSet.vulkanperf$getXMin();
			result = 31 * result + bitSet.vulkanperf$getYMin();
			result = 31 * result + bitSet.vulkanperf$getZMin();
			result = 31 * result + bitSet.vulkanperf$getXMax();
			result = 31 * result + bitSet.vulkanperf$getYMax();
			result = 31 * result + bitSet.vulkanperf$getZMax();
			return 31 * result + bitSet.vulkanperf$getStorage().hashCode();
		}
		return 31 * result + Objects.hashCode(shape);
	}

	@Override
	public boolean equals(DiscreteVoxelShape a, DiscreteVoxelShape b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null || a.getClass() != b.getClass()) {
			return false;
		}
		DiscreteVoxelShapeAccessor sizeA = (DiscreteVoxelShapeAccessor) a;
		DiscreteVoxelShapeAccessor sizeB = (DiscreteVoxelShapeAccessor) b;
		if (sizeA.vulkanperf$getXSize() != sizeB.vulkanperf$getXSize()
				|| sizeA.vulkanperf$getYSize() != sizeB.vulkanperf$getYSize()
				|| sizeA.vulkanperf$getZSize() != sizeB.vulkanperf$getZSize()) {
			return false;
		}
		if (a instanceof SubShapeAccessor subA) {
			SubShapeAccessor subB = (SubShapeAccessor) b;
			return subA.vulkanperf$getStartX() == subB.vulkanperf$getStartX()
					&& subA.vulkanperf$getStartY() == subB.vulkanperf$getStartY()
					&& subA.vulkanperf$getStartZ() == subB.vulkanperf$getStartZ()
					&& subA.vulkanperf$getEndX() == subB.vulkanperf$getEndX()
					&& subA.vulkanperf$getEndY() == subB.vulkanperf$getEndY()
					&& subA.vulkanperf$getEndZ() == subB.vulkanperf$getEndZ()
					&& equals(subA.vulkanperf$getParent(), subB.vulkanperf$getParent());
		} else if (a instanceof BitSetDiscreteVoxelShapeAccessor bitA) {
			BitSetDiscreteVoxelShapeAccessor bitB = (BitSetDiscreteVoxelShapeAccessor) b;
			return bitA.vulkanperf$getXMin() == bitB.vulkanperf$getXMin()
					&& bitA.vulkanperf$getYMin() == bitB.vulkanperf$getYMin()
					&& bitA.vulkanperf$getZMin() == bitB.vulkanperf$getZMin()
					&& bitA.vulkanperf$getXMax() == bitB.vulkanperf$getXMax()
					&& bitA.vulkanperf$getYMax() == bitB.vulkanperf$getYMax()
					&& bitA.vulkanperf$getZMax() == bitB.vulkanperf$getZMax()
					&& bitA.vulkanperf$getStorage().equals(bitB.vulkanperf$getStorage());
		}
		return a.equals(b);
	}
}
