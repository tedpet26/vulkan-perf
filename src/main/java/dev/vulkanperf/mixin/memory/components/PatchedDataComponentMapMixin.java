package dev.vulkanperf.mixin.memory.components;

import dev.vulkanperf.memory.components.EmptyPatchSharing;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Any patch that ends up empty (removed back to the prototype's defaults, or
 * never modified) is swapped for one shared immutable empty map instead of
 * keeping its own tiny per-instance map alive.
 */
@Mixin(PatchedDataComponentMap.class)
public abstract class PatchedDataComponentMapMixin {

	@Shadow
	private Reference2ObjectMap<DataComponentType<?>, Object> patch;

	@Shadow
	private boolean copyOnWrite;

	@Inject(
			method = {
					"applyPatch(Lnet/minecraft/core/component/DataComponentPatch;)V",
					"restorePatch",
					"clearPatch"
			},
			at = @At("RETURN")
	)
	private void vulkanperf$shareIfEmpty(CallbackInfo ci) {
		this.vulkanperf$replaceEmptyPatch();
	}

	@Inject(method = {"set", "remove"}, at = @At("RETURN"))
	private void vulkanperf$shareIfEmptyWithReturn(CallbackInfoReturnable<?> cir) {
		this.vulkanperf$replaceEmptyPatch();
	}

	@Unique
	private void vulkanperf$replaceEmptyPatch() {
		if (this.patch.isEmpty() && !this.copyOnWrite) {
			this.patch = EmptyPatchSharing.sharedEmptyPatch();
			this.copyOnWrite = true;
		}
	}
}
