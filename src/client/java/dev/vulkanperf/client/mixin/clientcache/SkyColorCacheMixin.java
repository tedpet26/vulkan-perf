package dev.vulkanperf.client.mixin.clientcache;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AtmosphericFogEnvironment.class)
public abstract class SkyColorCacheMixin {
	@Unique
	private Vector3fc vulkanperf$lastColor;
	@Unique
	private long vulkanperf$lastDayTime = Long.MIN_VALUE;
	@Unique
	private int vulkanperf$lastBlockX;
	@Unique
	private int vulkanperf$lastBlockZ;

	@Inject(method = "getBaseColor", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$reuseSky(ClientLevel level, Camera camera, int renderDistance, float partialTick, CallbackInfoReturnable<Vector3fc> cir) {
		if (!PerfConfig.get().clientcache.sky || level == null || camera == null || this.vulkanperf$lastColor == null) {
			return;
		}
		long dayTime = level.getOverworldClockTime();
		int blockX = camera.blockPosition().getX() >> 4;
		int blockZ = camera.blockPosition().getZ() >> 4;
		if (dayTime == this.vulkanperf$lastDayTime && blockX == this.vulkanperf$lastBlockX && blockZ == this.vulkanperf$lastBlockZ) {
			cir.setReturnValue(this.vulkanperf$lastColor);
		}
	}

	@Inject(method = "getBaseColor", at = @At("RETURN"))
	private void vulkanperf$storeSky(ClientLevel level, Camera camera, int renderDistance, float partialTick, CallbackInfoReturnable<Vector3fc> cir) {
		if (!PerfConfig.get().clientcache.sky || level == null || camera == null) {
			return;
		}
		this.vulkanperf$lastColor = cir.getReturnValue();
		this.vulkanperf$lastDayTime = level.getOverworldClockTime();
		this.vulkanperf$lastBlockX = camera.blockPosition().getX() >> 4;
		this.vulkanperf$lastBlockZ = camera.blockPosition().getZ() >> 4;
	}
}
