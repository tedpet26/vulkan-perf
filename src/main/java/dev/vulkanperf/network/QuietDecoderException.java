package dev.vulkanperf.network;

import io.netty.handler.codec.DecoderException;

/**
 * Decoder exception without a stack trace: frame-decode failures on the network thread are
 * frequent under attack traffic and {@code Throwable} fill-in dominates their cost.
 */
public class QuietDecoderException extends DecoderException {
	public QuietDecoderException(String message) {
		super(message);
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}
}
