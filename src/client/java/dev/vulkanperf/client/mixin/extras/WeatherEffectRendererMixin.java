package dev.vulkanperf.client.mixin.extras;

import com.mojang.renderpearl.api.commands.RenderPass;
import dev.vulkanperf.client.extras.ExtrasRuntime;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WeatherEffectRenderer.class)
public abstract class WeatherEffectRendererMixin {
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$weather(WeatherRenderState weatherRenderState, RenderPass pass, CallbackInfo ci) {
		if (!ExtrasRuntime.weather()) {
			ci.cancel();
		}
	}
}
