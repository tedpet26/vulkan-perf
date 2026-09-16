package dev.vulkanperf.client.mixin.particles;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {
	@Inject(method = "add(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$cullAdd(Particle particle, CallbackInfo ci) {
		if (!PerfConfig.get().particles.frustumCull || particle == null) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		if (client.gameRenderer == null) {
			return;
		}
		Camera camera = client.gameRenderer.mainCamera();
		var box = particle.getBoundingBox();
		double dx = box.getCenter().x - camera.position().x;
		double dy = box.getCenter().y - camera.position().y;
		double dz = box.getCenter().z - camera.position().z;
		double range = client.options.getEffectiveRenderDistance() * 16.0;
		if (dx * dx + dy * dy + dz * dz > range * range) {
			ci.cancel();
		}
	}
}
