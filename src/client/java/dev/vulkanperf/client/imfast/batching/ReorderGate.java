package dev.vulkanperf.client.imfast.batching;

import dev.vulkanperf.client.imfast.ImFastRuntime;

/** Always-on reorder flag for {@code RenderTypeFeatureRenderer$Group} when enhanced batching is active. */
public final class ReorderGate {
	private ReorderGate() {
	}

	public static boolean allowReorder(boolean vanilla) {
		return ImFastRuntime.enhancedBatching() || vanilla;
	}

	/**
	 * Records scissor transitions; an active scissor window makes draw-order
	 * changes unsafe, so the reorder gate closes while one is open.
	 */
	public static void noteScissor(boolean enabled) {
		SCISSOR_ACTIVE = enabled;
	}

	public static boolean scissorActive() {
		return SCISSOR_ACTIVE;
	}

	private static volatile boolean SCISSOR_ACTIVE;
}
