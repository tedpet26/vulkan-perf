package dev.vulkanperf.client.mixin.reloadui;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingOverlay.class)
public abstract class LoadingOverlayMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	@Final
	private ReloadInstance reload;

	@Unique
	private static boolean vulkanperf$finishedOnce;

	@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$skipLaterOverlays(CallbackInfo ci) {
		if (!PerfConfig.get().reloadui.skipAfterFirst) {
			return;
		}
		if (vulkanperf$finishedOnce) {
			this.minecraft.gui.setOverlay(null);
			ci.cancel();
		}
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void vulkanperf$markFinished(CallbackInfo ci) {
		if (this.reload.isDone()) {
			vulkanperf$finishedOnce = true;
		}
	}
}
