package dev.vulkanperf.client.imfast.sign;

/** Implemented on {@code SignText} to skip buffering for obfuscated / uncacheable text. */
public interface SignTextExtension {
	boolean vulkanperf$shouldCache();

	void vulkanperf$setShouldCache(boolean shouldCache);
}
