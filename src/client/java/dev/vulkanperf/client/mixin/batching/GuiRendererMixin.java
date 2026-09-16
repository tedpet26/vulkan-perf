package dev.vulkanperf.client.mixin.batching;

import dev.vulkanperf.client.batching.HudBatcher;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.3 vanilla already atlases and sorts GUI geometry in {@code GuiRenderer}.
 * This module tracks HUD prepare passes for diagnostics; heavy spreading lives in {@code hudspread}.
 */
@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
	@Inject(method = "prepare", at = @At("HEAD"))
	private void vulkanperf$beginHudPass(CallbackInfo ci) {
		HudBatcher.beginPass();
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void vulkanperf$endPass(CallbackInfo ci) {
		HudBatcher.endPass();
	}
}
