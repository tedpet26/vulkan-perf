package dev.vulkanperf.client.mixin.extras;

import dev.vulkanperf.client.extras.ExtrasRuntime;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineFilterMixin {
	@Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$filter(ParticleOptions options, double x, double y, double z, double xd, double yd, double zd, CallbackInfoReturnable<Particle> cir) {
		if (!ExtrasRuntime.particles()) {
			cir.setReturnValue(null);
			return;
		}
		Identifier id = BuiltInRegistries.PARTICLE_TYPE.getKey(options.getType());
		if (id != null) {
			String path = id.getPath();
			if ((path.contains("rain") || path.contains("splash") || path.contains("cloud")) && !ExtrasRuntime.weatherParticles()) {
				cir.setReturnValue(null);
			}
		}
	}
}
