package dev.vulkanperf.client.imfast.batching;

import dev.vulkanperf.client.imfast.ImFastRuntime;

/** Always-on reorder flag for {@code RenderTypeFeatureRenderer$Group} when enhanced batching is active. */
public final class ReorderGate {
	private ReorderGate() {
	}

	public static boolean allowReorder(boolean vanilla) {
		return ImFastRuntime.enhancedBatching() || vanilla;
	}
}
