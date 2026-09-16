package dev.vulkanperf.client.mixin.culling;

import dev.vulkanperf.client.culling.EntityOcclusionCuller;
import dev.vulkanperf.config.PerfConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
	@Inject(
		method = "shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDDF)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void vulkanperf$occlusionCull(Entity entity, Frustum frustum, double camX, double camY, double camZ, float partialTicks, CallbackInfoReturnable<Boolean> cir) {
		if (!PerfConfig.get().culling.enabled || !PerfConfig.get().culling.entities || entity == null) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		if (client.gameRenderer == null) {
			return;
		}
		Camera camera = client.gameRenderer.mainCamera();
		if (!EntityOcclusionCuller.isVisible(entity, camera)) {
			cir.setReturnValue(false);
		}
	}
}
