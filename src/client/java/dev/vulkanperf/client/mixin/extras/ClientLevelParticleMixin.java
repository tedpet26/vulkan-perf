package dev.vulkanperf.client.mixin.extras;

import dev.vulkanperf.client.extras.ExtrasRuntime;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelParticleMixin {
	@Inject(method = "addDestroyBlockEffect", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$blockBreak(BlockPos pos, BlockState state, CallbackInfo ci) {
		if (!ExtrasRuntime.blockBreak()) {
			ci.cancel();
		}
	}

	@Inject(method = "addBreakingBlockEffects", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$blockBreaking(BlockPos pos, Direction direction, boolean sound, CallbackInfo ci) {
		if (!ExtrasRuntime.blockBreaking()) {
			ci.cancel();
		}
	}

	@Redirect(method = "tickWeatherEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
	private void vulkanperf$rainSplash(ClientLevel instance, ParticleOptions options, double x, double y, double z, double xd, double yd, double zd) {
		if (ExtrasRuntime.rainSplash()) {
			instance.addParticle(options, x, y, z, xd, yd, zd);
		}
	}
}
