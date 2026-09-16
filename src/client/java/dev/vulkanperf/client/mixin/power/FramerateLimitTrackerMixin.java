package dev.vulkanperf.client.mixin.power;

import com.mojang.blaze3d.platform.FramerateLimitTracker;
import dev.vulkanperf.client.power.PowerController;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FramerateLimitTracker.class)
public abstract class FramerateLimitTrackerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "getFramerateLimit", at = @At("RETURN"), cancellable = true)
	private void vulkanperf$cap(CallbackInfoReturnable<Integer> cir) {
		cir.setReturnValue(PowerController.frameCap(this.minecraft, cir.getReturnValueI()));
	}
}
