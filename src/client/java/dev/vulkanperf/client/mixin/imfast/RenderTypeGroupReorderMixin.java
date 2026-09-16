package dev.vulkanperf.client.mixin.imfast;

import dev.vulkanperf.client.imfast.batching.ReorderGate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer$Group")
public abstract class RenderTypeGroupReorderMixin {
	@ModifyVariable(method = "<init>", at = @At("HEAD"), name = "canReorder", argsOnly = true)
	private static boolean vulkanperf$forceReorder(boolean canReorder) {
		return ReorderGate.allowReorder(canReorder);
	}
}
