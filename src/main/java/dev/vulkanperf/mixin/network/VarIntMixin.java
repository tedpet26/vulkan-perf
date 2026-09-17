package dev.vulkanperf.mixin.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import dev.vulkanperf.network.VarIntSizes;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.VarInt;

/**
 * VarInt encode fast paths: byte-size via leading-zero table, 1/2-byte writes peeled to single
 * primitive buffer writes so the JIT can inline them.
 */
@Mixin(VarInt.class)
public abstract class VarIntMixin {

	/**
	 * @reason table lookup replaces the shift loop
	 * @author vulkan-perf
	 */
	@Overwrite
	public static int getByteSize(final int value) {
		return VarIntSizes.of(value);
	}

	/**
	 * @reason peel the common 1/2-byte cases
	 * @author vulkan-perf
	 */
	@Overwrite
	public static ByteBuf write(final ByteBuf output, int value) {
		int byteSize = VarIntSizes.of(value);
		if (byteSize == 1) {
			output.writeByte(value);
			return output;
		}
		if (byteSize == 2) {
			output.writeShort(((value & 0x7F | 0x80) << 8) | ((value >>> 7) & 0x7F));
			return output;
		}
		while ((value & -128) != 0) {
			output.writeByte(value & 127 | 128);
			value >>>= 7;
		}
		output.writeByte(value);
		return output;
	}
}
