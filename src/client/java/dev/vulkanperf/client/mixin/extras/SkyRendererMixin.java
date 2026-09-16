package dev.vulkanperf.client.mixin.extras;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.commands.RenderPass;
import dev.vulkanperf.client.extras.ExtrasRuntime;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.MoonPhase;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
	@Inject(method = "renderSkyDisc", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$skyDisc(RenderPass pass, Vector3fc color, CallbackInfo ci) {
		if (!ExtrasRuntime.sky()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderEndSky", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$endSky(RenderPass pass, CallbackInfo ci) {
		if (!ExtrasRuntime.sky()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderSun", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$sun(RenderPass pass, float rain, PoseStack poseStack, CallbackInfo ci) {
		if (!ExtrasRuntime.sun()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderMoon", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$moon(RenderPass pass, MoonPhase phase, float rain, PoseStack poseStack, CallbackInfo ci) {
		if (!ExtrasRuntime.moon()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderStars", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$stars(RenderPass pass, float rain, PoseStack poseStack, CallbackInfo ci) {
		if (!ExtrasRuntime.stars()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderSunriseAndSunset", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$sunrise(RenderPass pass, PoseStack poseStack, float rain, Vector4fc color, CallbackInfo ci) {
		if (!ExtrasRuntime.sun()) {
			ci.cancel();
		}
	}
}
