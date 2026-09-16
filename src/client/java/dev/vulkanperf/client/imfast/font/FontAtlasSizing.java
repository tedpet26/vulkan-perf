package dev.vulkanperf.client.imfast.font;

import dev.vulkanperf.client.imfast.ImFastRuntime;

/** Resolves the glyph-atlas edge length used by {@code FontTexture} mixins. */
public final class FontAtlasSizing {
	public static final int VANILLA_SIZE = 256;

	private FontAtlasSizing() {
	}

	public static int size() {
		return ImFastRuntime.fontAtlasResizing() ? ImFastRuntime.fontAtlasSize() : VANILLA_SIZE;
	}

	public static float sizeF() {
		return size();
	}
}
