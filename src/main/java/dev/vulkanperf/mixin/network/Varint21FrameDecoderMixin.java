package dev.vulkanperf.mixin.network;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Overwrite;

import dev.vulkanperf.network.QuietDecoderException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.BandwidthDebugMonitor;
import net.minecraft.network.Varint21FrameDecoder;

/**
 * Frame decoder fast path: leading NUL padding from legacy pings is skipped instead of decoded
 * (blocks the classic zero-length frame flood), the length varint is decoded in place without a
 * helper buffer, and payloads are taken as retained slices rather than copied into fresh
 * buffers. Failure paths throw stack-trace-free exceptions.
 */
@Mixin(Varint21FrameDecoder.class)
public abstract class Varint21FrameDecoderMixin {
	@Shadow
	private BandwidthDebugMonitor monitor;

	/**
	 * @reason replace per-byte helper-buffer decode with in-place decode
	 * @author vulkan-perf
	 */
	@Overwrite
	protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
		// Drop runs of NUL padding (legacy ping flood); varints never start with 0x00 except
		// for invalid zero-length frames, which vanilla rejects anyway.
		int firstNonNul = in.forEachByte(io.netty.util.ByteProcessor.FIND_NON_NUL);
		if (firstNonNul == -1) {
			in.skipBytes(in.readableBytes());
			return;
		}
		in.readerIndex(firstNonNul);
		in.markReaderIndex();

		int length = vp$readVarInt21(in);
		if (length < 0) {
			in.resetReaderIndex();
			return;
		}
		if (length == 0) {
			throw new QuietDecoderException("Frame length cannot be zero");
		}
		if (in.readableBytes() < length) {
			in.resetReaderIndex();
			return;
		}
		if (this.monitor != null) {
			this.monitor.onReceive(length + net.minecraft.network.VarInt.getByteSize(length));
		}
		out.add(in.readRetainedSlice(length));
	}

	/**
	 * Reads a 1-3 byte varint without consuming on partial input; -1 means "needs more bytes".
	 */
	private static int vp$readVarInt21(ByteBuf in) {
		int readable = in.readableBytes();
		if (readable == 0) {
			return -1;
		}
		int first = in.getUnsignedByte(in.readerIndex());
		if ((first & 0x80) == 0) {
			in.skipBytes(1);
			return first;
		}
		if (readable == 1) {
			return -1;
		}
		int second = in.getUnsignedByte(in.readerIndex() + 1);
		if ((second & 0x80) == 0) {
			in.skipBytes(2);
			return (first & 0x7F) | (second & 0x7F) << 7;
		}
		if (readable == 2) {
			return -1;
		}
		int third = in.getUnsignedByte(in.readerIndex() + 2);
		if ((third & 0x80) == 0) {
			in.skipBytes(3);
			return (first & 0x7F) | (second & 0x7F) << 7 | (third & 0x7F) << 14;
		}
		throw new QuietDecoderException("length wider than 21-bit");
	}
}
