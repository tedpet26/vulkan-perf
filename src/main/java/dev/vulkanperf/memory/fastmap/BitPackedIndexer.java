package dev.vulkanperf.memory.fastmap;

import net.minecraft.util.Mth;

/**
 * A {@link PropertyIndexer} that reserves a power-of-two sized bit-field for a
 * property's values. This wastes some index slots when a property's value
 * count is not itself a power of two, but every operation reduces to shifts
 * and masks instead of integer division, which is significantly faster.
 */
public record BitPackedIndexer(int valueCount, int lowBit, int bitWidth) implements PropertyIndexer {

	public static BitPackedIndexer of(int spanBefore, int valueCount) {
		if (!Mth.isPowerOfTwo(spanBefore)) {
			throw new IllegalArgumentException("Span before a bit-packed property must be a power of two, was " + spanBefore);
		}
		int paddedCount = Mth.smallestEncompassingPowerOfTwo(valueCount);
		int lowBit = Mth.log2(spanBefore);
		int width = Mth.log2(paddedCount);
		if (lowBit + width > 31) {
			throw new IllegalStateException("Too many property bits to pack into a 32-bit index");
		}
		return new BitPackedIndexer(valueCount, lowBit, width);
	}

	@Override
	public int moveTo(int index, int valueIndex) {
		if (valueIndex < 0 || valueIndex >= valueCount) {
			return -1;
		}
		int keepMask = ~lowMask(lowBit + bitWidth) | lowMask(lowBit);
		return (index & keepMask) | baseOffset(valueIndex);
	}

	@Override
	public int baseOffset(int valueIndex) {
		return valueIndex << lowBit;
	}

	@Override
	public int span() {
		return 1 << bitWidth;
	}

	@Override
	public int extract(int index) {
		return (index >>> lowBit) & lowMask(bitWidth);
	}

	private static int lowMask(int bits) {
		return bits >= Integer.SIZE ? -1 : (1 << bits) - 1;
	}
}
