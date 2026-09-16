package dev.vulkanperf.client.mixin.extras;

import dev.vulkanperf.client.extras.ExtrasRuntime;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AtmosphericFogEnvironment.class)
public abstract class SkyColorsMixin {
	@Unique
	private static final Vector3fc VULKANPERF$FLAT_SKY = new Vector3f(
		((7907327 >> 16) & 255) / 255.0f,
		((7907327 >> 8) & 255) / 255.0f,
		(7907327 & 255) / 255.0f
	);

	@Inject(method = "getBaseColor", at = @At("RETURN"), cancellable = true)
	private void vulkanperf$skyColor(CallbackInfoReturnable<Vector3fc> cir) {
		if (!ExtrasRuntime.skyColors()) {
			cir.setReturnValue(VULKANPERF$FLAT_SKY);
		}
	}
}
