package dev.vulkanperf.client.imfast.batching;

import dev.vulkanperf.client.imfast.ImFastRuntime;

/**
 * Gate for draw-consolidation widening in {@code RenderTypeFeatureRenderer$Group}.
 * An active scissor window makes draw-order/draw-boundary changes unsafe, so the
 * gate closes while one is open (tracked by {@code ScissorStateMixin}).
 */
public final class ReorderGate {
	private ReorderGate() {
	}

	public static boolean allowReorder(boolean vanilla) {
		return vanilla || (ImFastRuntime.enhancedBatching() && !SCISSOR_ACTIVE);
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
