package dev.vulkanperf.client.mixin.particles;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.vulkanperf.client.particles.ParticleCullingState;
import dev.vulkanperf.config.PerfConfig;
import net.minecraft.client.particle.QuadParticleGroup;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.culling.Frustum;

/**
 * Particle extract culling (Particle Core class): the vanilla point test culls small particles
 * whose center leaves the frustum but keeps large ones fully in the pipeline even when only a
 * corner peeks in; a bounding-box test plus a render-distance gate trims both directions.
 */
@Mixin(QuadParticleGroup.class)
public abstract class QuadParticleGroupCullMixin {

	@Redirect(
		method = "extractRenderState",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/culling/Frustum;pointInFrustum(DDD)Z")
	)
	private static boolean vp$shouldKeepParticle(Frustum frustum, double x, double y, double z, @Local(ordinal = 0) SingleQuadParticle particle) {
		PerfConfig.ParticlesConfig cfg = PerfConfig.get().particles;
		if (cfg.renderDistanceCull && !ParticleCullingState.withinRenderDistance(x, y, z)) {
			return false;
		}
		if (cfg.frustumBoundingBox) {
			return frustum.isVisible(particle.getBoundingBox());
		}
		return frustum.pointInFrustum(x, y, z);
	}
}
