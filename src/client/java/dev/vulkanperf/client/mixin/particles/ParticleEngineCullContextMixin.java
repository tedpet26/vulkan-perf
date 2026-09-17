package dev.vulkanperf.client.mixin.particles;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.vulkanperf.client.particles.ParticleCullingState;
import dev.vulkanperf.config.PerfConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.ParticlesRenderState;

/** Captures the per-frame culling context once instead of per particle. */
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineCullContextMixin {

	@Inject(method = "extract", at = @At("HEAD"))
	private void vp$beginCullFrame(ParticlesRenderState state, Frustum frustum, Camera camera, float partialTicks, CallbackInfo ci) {
		PerfConfig.ParticlesConfig cfg = PerfConfig.get().particles;
		if (cfg.enabled && (cfg.frustumBoundingBox || cfg.renderDistanceCull)) {
			ParticleCullingState.beginFrame(frustum, camera, cfg.renderDistanceMultiplier);
		}
	}
}
