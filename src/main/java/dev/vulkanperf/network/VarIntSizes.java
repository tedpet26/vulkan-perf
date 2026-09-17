package dev.vulkanperf.network;

/**
 * Byte-size table for protocol varints, indexed by {@code Integer.numberOfLeadingZeros}.
 * Replaces the shift-mask loop on the packet encode hot path.
 */
public final class VarIntSizes {
	private static final int[] BYTE_SIZES = new int[33];

	static {
		for (int leadingZeros = 0; leadingZeros <= 32; leadingZeros++) {
			int significantBits = 32 - leadingZeros;
			int bytes = (significantBits + 6) / 7;
			if (bytes < 1) {
				bytes = 1;
			}
			if (bytes > 5) {
				bytes = 5;
			}
			BYTE_SIZES[leadingZeros] = bytes;
		}
	}

	private VarIntSizes() {
	}

	public static int of(int value) {
		return BYTE_SIZES[Integer.numberOfLeadingZeros(value)];
	}
}
