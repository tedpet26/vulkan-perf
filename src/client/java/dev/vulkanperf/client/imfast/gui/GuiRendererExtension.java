package dev.vulkanperf.client.imfast.gui;

import org.jspecify.annotations.Nullable;

/** Implemented on {@code GuiRenderer} so the debug overlay can report the animated item atlas. */
public interface GuiRendererExtension {
	@Nullable
	AnimatedItemAtlas vulkanperf$animatedItemAtlas();
}
