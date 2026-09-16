package dev.vulkanperf.client.mixin.imfast;

import com.mojang.blaze3d.systems.ScissorState;
import dev.vulkanperf.client.imfast.batching.ReorderGate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tracks scissor enable/disable so the render-type group reorder can tell
 * when reordering is unsafe (26.3 ScissorState has no equality to overwrite,
 * so we observe transitions instead of turning the state into a key).
 */
@Mixin(ScissorState.class)
public abstract class ScissorStateMixin {
	@Shadow
	private boolean enabled;

	@Inject(method = "enable", at = @At("RETURN"))
	private void vulkanperf$onEnable(int x, int y, int width, int height, CallbackInfo ci) {
		ReorderGate.noteScissor(true);
	}

	@Inject(method = "disable", at = @At("RETURN"))
	private void vulkanperf$onDisable(CallbackInfo ci) {
		ReorderGate.noteScissor(false);
	}
}
