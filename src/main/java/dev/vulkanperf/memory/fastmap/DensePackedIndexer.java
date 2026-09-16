package dev.vulkanperf.memory.fastmap;

/**
 * A {@link PropertyIndexer} that packs a property's values as densely as
 * possible (no wasted slots), at the cost of using integer division/modulo
 * instead of bit operations. Opt-in via {@code compactFastMap} since it
 * trades a small amount of CPU for a smaller value matrix.
 */
public record DensePackedIndexer(int stride, int valueCount) implements PropertyIndexer {

	@Override
	public int moveTo(int index, int valueIndex) {
		if (valueIndex < 0 || valueIndex >= valueCount) {
			return -1;
		}
		int strideAfter = stride * valueCount;
		int low = index % stride;
		int high = index - index % strideAfter;
		return low + baseOffset(valueIndex) + high;
	}

	@Override
	public int baseOffset(int valueIndex) {
		return stride * valueIndex;
	}

	@Override
	public int span() {
		return valueCount;
	}

	@Override
	public int extract(int index) {
		return (index / stride) % valueCount;
	}
}
