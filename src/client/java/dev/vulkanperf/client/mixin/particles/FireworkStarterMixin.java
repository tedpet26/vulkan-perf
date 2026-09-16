package dev.vulkanperf.client.mixin.particles;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code FireworkParticles.Starter.createParticle} is the one vanilla caller that
 * dereferences {@link ParticleEngine#createParticle}'s result without a null check.
 * A null result is legal — providers can be missing (reload races, pack swaps) and
 * particle filters legitimately refuse creation — and crashing the render thread
 * with an NPE mid-tick is strictly worse than dropping one spark. Replicates the
 * vanilla spark setup but skips it entirely when no particle was produced.
 */
@Mixin(FireworkParticles.Starter.class)
public abstract class FireworkStarterMixin extends Particle {
	protected FireworkStarterMixin(ClientLevel level, double x, double y, double z) {
		super(level, x, y, z);
	}

	@Shadow
	@Final
	private ParticleEngine engine;

	@Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$nullSafeCreateParticle(double x, double y, double z, double vx, double vy, double vz,
		IntList colors, IntList fadeColors, boolean trail, boolean twinkle, CallbackInfo ci) {
		Particle particle = this.engine.createParticle(ParticleTypes.FIREWORK, x, y, z, vx, vy, vz);
		if (particle == null) {
			ci.cancel();
			return;
		}
		SparkParticleInvoker spark = (SparkParticleInvoker) particle;
		spark.vulkanperf$setTrail(trail);
		spark.vulkanperf$setTwinkle(twinkle);
		spark.vulkanperf$setAlpha(0.99F);
		spark.vulkanperf$setColor(Util.getRandom(colors, this.random));
		if (!fadeColors.isEmpty()) {
			spark.vulkanperf$setFadeColor(Util.getRandom(fadeColors, this.random));
		}
		ci.cancel();
	}
}
