package dev.vulkanperf.client.mixin.imfast;

import dev.vulkanperf.client.imfast.font.FontAtlasSizing;
import net.minecraft.client.gui.font.FontTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FontTexture.class)
public abstract class FontTextureMixin {
	@Unique
	private int vulkanperf$atlasSize = FontAtlasSizing.VANILLA_SIZE;

	@Inject(method = "<init>", at = @At("CTOR_HEAD"))
	private void vulkanperf$cacheAtlasSize(CallbackInfo ci) {
		this.vulkanperf$atlasSize = FontAtlasSizing.size();
	}

	@ModifyConstant(method = "*", constant = @Constant(intValue = 256))
	private int vulkanperf$resizeAtlasInt(int original) {
		return this.vulkanperf$atlasSize;
	}

	@SuppressWarnings("MixinAnnotationTarget")
	@ModifyConstant(method = "*", constant = @Constant(floatValue = 256.0F))
	private float vulkanperf$resizeAtlasFloat(float original) {
		return this.vulkanperf$atlasSize;
	}
}
