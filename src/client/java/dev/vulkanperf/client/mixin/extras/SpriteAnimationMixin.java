package dev.vulkanperf.client.mixin.extras;

import dev.vulkanperf.client.extras.AnimationSprites;
import dev.vulkanperf.client.extras.ExtrasRuntime;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteContents.AnimationState.class)
public abstract class SpriteAnimationMixin {
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$animation(CallbackInfo ci) {
		if (!ExtrasRuntime.animate(AnimationSprites.id(this))) {
			ci.cancel();
		}
	}
}
