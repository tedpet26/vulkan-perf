package dev.vulkanperf.client.mixin.extras;

import dev.vulkanperf.client.extras.ExtrasRuntime;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
	@Inject(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$nametag(LivingEntity entity, double distance, CallbackInfoReturnable<Boolean> cir) {
		if (entity instanceof AbstractClientPlayer && !ExtrasRuntime.nametags()) {
			cir.setReturnValue(false);
		}
	}
}
