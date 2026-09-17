package dev.vulkanperf.client.mixin.imfast;

import dev.vulkanperf.client.imfast.GuiIntersectionIndex;
import dev.vulkanperf.client.imfast.ImFastRuntime;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.ScreenArea;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Fast path for {@code GuiRenderState#hasIntersection}: when the probed rectangle falls
 * outside the union of everything ever added to that list, the vanilla per-element scan
 * cannot find an intersection and is skipped entirely. See {@link GuiIntersectionIndex}.
 */
@Mixin(GuiRenderState.class)
public abstract class GuiRenderStateIntersectionMixin {
	@Inject(method = "reset", at = @At("HEAD"))
	private void vulkanperf$clearUnions(CallbackInfo ci) {
		GuiIntersectionIndex.clear();
	}

	@Inject(method = "hasIntersection", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$fastReject(ScreenRectangle rect, List<? extends ScreenArea> list, CallbackInfoReturnable<Boolean> cir) {
		if (rect != null && ImFastRuntime.guiIntersectionFastPath() && GuiIntersectionIndex.cannotIntersect(list, rect)) {
			cir.setReturnValue(false);
		}
	}
}
