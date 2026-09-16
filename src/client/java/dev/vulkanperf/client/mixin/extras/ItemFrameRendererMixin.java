package dev.vulkanperf.client.mixin.extras;

import dev.vulkanperf.client.extras.ExtrasRuntime;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrameRenderer.class)
public abstract class ItemFrameRendererMixin {
	@Inject(method = "submit", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$itemFrames(CallbackInfo ci) {
		if (!ExtrasRuntime.itemFrames()) {
			ci.cancel();
		}
	}

	@Inject(method = "shouldShowName(Lnet/minecraft/world/entity/decoration/ItemFrame;D)Z", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$nametag(ItemFrame frame, double distance, CallbackInfoReturnable<Boolean> cir) {
		if (!ExtrasRuntime.itemFrameNametags()) {
			cir.setReturnValue(false);
		}
	}
}
