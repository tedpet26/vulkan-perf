package dev.vulkanperf.client.mixin.extras;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import dev.vulkanperf.client.extras.AnimationSprites;
import dev.vulkanperf.client.extras.ExtrasRuntime;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TextureAtlas.class)
public abstract class TextureAtlasAnimationMixin {
	@WrapWithCondition(method = "cycleAnimationFrames", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/SpriteContents$AnimationState;tick()V"))
	private boolean vulkanperf$shouldAnimate(SpriteContents.AnimationState state) {
		return ExtrasRuntime.animate(AnimationSprites.id(state));
	}

	@WrapOperation(method = "upload", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;createAnimationState(Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;I)Lnet/minecraft/client/renderer/texture/SpriteContents$AnimationState;"))
	private SpriteContents.AnimationState vulkanperf$bindSprite(TextureAtlasSprite sprite, GpuBufferSlice slice, int mip, Operation<SpriteContents.AnimationState> original) {
		SpriteContents.AnimationState state = original.call(sprite, slice, mip);
		if (sprite != null && sprite.contents() != null) {
			AnimationSprites.bind(state, sprite.contents().name());
		}
		return state;
	}
}
