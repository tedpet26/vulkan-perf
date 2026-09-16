package dev.vulkanperf.client.imfast.batching;

import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;

import java.util.List;
import java.util.Objects;

/**
 * Comparability check for draw consolidation inside a reorderable
 * {@code RenderTypeFeatureRenderer$Group}.
 *
 * <p>Vanilla only consolidates a new draw into an earlier one when the
 * {@link PreparedRenderType} records compare equal, which pins the bound
 * pipeline. When enhanced batching widens that window to any equal
 * {@code RenderType} seen earlier in the group, pipelines with different
 * color target configurations could end up bound inside the same render
 * pass; the Vulkan frontend rejects that with
 * "Render pass color attachment count must match pipeline color target
 * state count" (and leaves debug groups open as a side effect of the
 * abort). This gate only permits the widened consolidation when the
 * candidate's pipeline/scissor/textures match the draw it would merge
 * into, so anything that would change pass state falls back to a fresh
 * draw exactly as vanilla would do.
 */
public final class RenderPipelineTargets {
	private RenderPipelineTargets() {
	}

	/**
	 * @param mergeTarget prepared render type of the draw the candidate would consolidate into
	 * @param candidate   prepared render type the group wants to place
	 * @return {@code true} when merging cannot change render-pass state
	 */
	public static boolean canMerge(PreparedRenderType mergeTarget, PreparedRenderType candidate) {
		if (mergeTarget == null || candidate == null) {
			return true;
		}
		return sameColorTargets(mergeTarget.pipeline(), candidate.pipeline())
			&& Objects.equals(mergeTarget.scissorState(), candidate.scissorState())
			&& Objects.equals(mergeTarget.dynamicTransforms(), candidate.dynamicTransforms())
			&& Objects.equals(mergeTarget.textures(), candidate.textures());
	}

	private static boolean sameColorTargets(RenderPipeline previous, RenderPipeline candidate) {
		if (previous == candidate) {
			return true;
		}
		if (previous == null || candidate == null) {
			return false;
		}
		List<ColorTargetState> a = previous.getColorTargetStates();
		List<ColorTargetState> b = candidate.getColorTargetStates();
		if (a.size() != b.size()) {
			return false;
		}
		for (int i = 0; i < a.size(); i++) {
			if (!sameTarget(a.get(i), b.get(i))) {
				return false;
			}
		}
		return true;
	}

	private static boolean sameTarget(ColorTargetState previous, ColorTargetState candidate) {
		if (previous == candidate) {
			return true;
		}
		if (previous == null || candidate == null) {
			return false;
		}
		return previous.format() == candidate.format()
			&& Objects.equals(previous.blendFunction(), candidate.blendFunction());
	}
}
