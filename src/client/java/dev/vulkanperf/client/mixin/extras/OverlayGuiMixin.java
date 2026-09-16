package dev.vulkanperf.client.mixin.extras;

import com.llamalad7.mixinextras.sugar.Local;
import dev.vulkanperf.client.extras.ExtrasHud;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class OverlayGuiMixin {
	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void vulkanperf$drawOverlay(DeltaTracker deltaTracker, boolean shouldRenderLevel, boolean resourcesLoaded, CallbackInfo ci, @Local GuiGraphicsExtractor graphics) {
		if (shouldRenderLevel) {
			ExtrasHud.draw(graphics);
		}
	}
}
