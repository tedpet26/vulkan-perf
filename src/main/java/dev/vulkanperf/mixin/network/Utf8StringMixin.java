package dev.vulkanperf.mixin.network;

import java.nio.charset.StandardCharsets;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.Utf8String;
import net.minecraft.network.VarInt;

/**
 * String writes go straight into the target buffer instead of staging through a temporary one:
 * {@code utf8Bytes} gives the exact encoded size up front, then {@code writeCharSequence} does
 * a single pass. Removes one buffer allocation + copy per written string.
 */
@Mixin(Utf8String.class)
public abstract class Utf8StringMixin {

	/**
	 * @reason single-pass write without a temp buffer
	 * @author vulkan-perf
	 */
	@Overwrite
	public static void write(final ByteBuf output, final CharSequence value, final int maxLength) {
		if (value.length() > maxLength) {
			throw new EncoderException("String too big (was " + value.length() + " characters, max " + maxLength + ")");
		}
		int encodedLength = ByteBufUtil.utf8Bytes(value);
		int maxAllowedEncodedLength = ByteBufUtil.utf8MaxBytes(maxLength);
		if (encodedLength > maxAllowedEncodedLength) {
			throw new EncoderException("String too big (was " + encodedLength + " bytes encoded, max " + maxAllowedEncodedLength + ")");
		}
		VarInt.write(output, encodedLength);
		output.writeCharSequence(value, StandardCharsets.UTF_8);
	}
}
