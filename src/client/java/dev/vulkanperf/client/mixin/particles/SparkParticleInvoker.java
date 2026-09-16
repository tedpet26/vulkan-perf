package dev.vulkanperf.client.mixin.particles;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Invoker view of the private {@code FireworkParticles.SparkParticle} config
 * methods, so {@link FireworkStarterMixin} can set them on the created particle.
 */
@Mixin(targets = "net.minecraft.client.particle.FireworkParticles$SparkParticle")
public interface SparkParticleInvoker {
	@Invoker("setTrail")
	void vulkanperf$setTrail(boolean trail);

	@Invoker("setTwinkle")
	void vulkanperf$setTwinkle(boolean twinkle);

	@Invoker("setColor")
	void vulkanperf$setColor(int color);

	@Invoker("setFadeColor")
	void vulkanperf$setFadeColor(int color);

	@Invoker("setAlpha")
	void vulkanperf$setAlpha(float alpha);
}
