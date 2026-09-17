package dev.vulkanperf.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.VarInt;

/**
 * Shared, stateless frame prepender: one instance serves every connection instead of one
 * handler allocation per channel (see {@code Connection#createFrameEncoder}).
 */
@Sharable
public final class Varint21Prepender extends MessageToByteEncoder<ByteBuf> {
	public static final Varint21Prepender INSTANCE = new Varint21Prepender();

	private Varint21Prepender() {
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {
		int bodyLength = msg.readableBytes();
		int headerLength = VarInt.getByteSize(bodyLength);
		if (headerLength > 3) {
			throw new EncoderException("Packet too large: size " + bodyLength + " is over 21 bits");
		}
		out.ensureWritable(headerLength + bodyLength);
		VarInt.write(out, bodyLength);
		out.writeBytes(msg, msg.readerIndex(), bodyLength);
	}
}
