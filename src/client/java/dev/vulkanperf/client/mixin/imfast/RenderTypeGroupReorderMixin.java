package dev.vulkanperf.client.mixin.imfast;

import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.vulkanperf.client.imfast.batching.RenderPipelineTargets;
import dev.vulkanperf.client.imfast.batching.ReorderGate;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/**
 * Enhanced batching for {@code RenderTypeFeatureRenderer$Group}, guarded.
 *
 * <p>Vanilla groups carry {@code canReorder = !strictlyOrdered}; a reorderable
 * group consolidates a new draw into an earlier one only when the prepared
 * render types compare equal, which pins the bound pipeline, scissor window,
 * transform slice and textures. Two changes here:
 *
 * <ul>
 *   <li>The constructor flag is forced open while the {@link ReorderGate} is
 *       open (feature enabled and no scissor window active), so strictly
 *       ordered groups (nametags, HUD-anchored world text, ...) can also
 *       consolidate equal consecutive draws.</li>
 *   <li>The widened consolidation (a re-prepared {@code RenderType} merging
 *       into the earlier draw of the same type) is additionally checked by
 *       {@link RenderPipelineTargets#canMerge}: re-preparation can carry a
 *       different pipeline or dynamic state, and merging those used to bind a
 *       pipeline whose color target configuration mismatched the render pass,
 *       crashing the Vulkan frontend with "Render pass color attachment count
 *       must match pipeline color target state count" (leaving debug groups
 *       open as a side effect of the abort). Incompatible merges are made to
 *       miss the lookup, which falls back to vanilla's fresh-draw path.</li>
 * </ul>
 */
@Mixin(targets = "net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer$Group")
public abstract class RenderTypeGroupReorderMixin {
	@ModifyVariable(method = "<init>", at = @At("HEAD"), name = "canReorder", argsOnly = true)
	private static boolean vulkanperf$forceReorder(boolean canReorder) {
		return ReorderGate.allowReorder(canReorder);
	}

	@WrapOperation(
		method = "getOrAddDraw",
		at = @At(value = "INVOKE", target = "Ljava/util/List;indexOf(Ljava/lang/Object;)I", remap = false)
	)
	private int vulkanperf$guardWidenedMerge(List<PreparedRenderType> drawRenderTypes, Object candidate, Operation<Integer> original) {
		int index = original.call(drawRenderTypes, candidate);
		if (index < 0) {
			return -1;
		}
		PreparedRenderType mergeTarget = drawRenderTypes.get(index);
		if (mergeTarget == candidate || RenderPipelineTargets.canMerge(mergeTarget, (PreparedRenderType) candidate)) {
			return index;
		}
		// Same RenderType re-prepared with different pass state; force a fresh draw.
		return -1;
	}
}
