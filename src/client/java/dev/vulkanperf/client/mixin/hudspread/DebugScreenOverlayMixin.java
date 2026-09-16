package dev.vulkanperf.client.mixin.hudspread;

import dev.vulkanperf.client.hudspread.HudSpreader;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugScreenOverlay.class)
public abstract class DebugScreenOverlayMixin {
	@Inject(method = "clearChunkCache", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$spreadChunkCache(CallbackInfo ci) {
		HudSpreader.tick();
		if (!HudSpreader.shouldExtractHeavyHud()) {
			ci.cancel();
		}
	}
}
