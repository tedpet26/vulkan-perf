package dev.vulkanperf.client.mixin.extras.sodium;

import dev.vulkanperf.client.extras.ExtrasRuntime;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderSectionManager.class)
public abstract class SodiumRenderSectionManagerMixin {
	@Shadow
	private float getRenderDistance() {
		throw new AssertionError();
	}

	@Inject(method = "getEffectiveRenderDistance", at = @At("RETURN"), cancellable = true)
	private void vulkanperf$disableFogCull(CallbackInfoReturnable<Float> cir) {
		if (!ExtrasRuntime.fog()) {
			cir.setReturnValue(this.getRenderDistance());
		}
	}

	@Inject(method = "getSearchDistance", at = @At("RETURN"), cancellable = true)
	private void vulkanperf$disableFogSearch(CallbackInfoReturnable<Float> cir) {
		if (!ExtrasRuntime.fog()) {
			cir.setReturnValue(this.getRenderDistance());
		}
	}
}
