package dev.vulkanperf.client.mixin.imfast;

import dev.vulkanperf.client.imfast.sign.SignTextExtension;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.entity.SignText;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SignText.class)
public abstract class SignTextMixin implements SignTextExtension {
	@Shadow
	private FormattedCharSequence @Nullable [] renderMessages;

	@Unique
	private boolean vulkanperf$shouldCache;
	@Unique
	private boolean vulkanperf$checkedCache;

	@Inject(method = "getRenderMessages", at = @At("RETURN"))
	private void vulkanperf$detectObfuscation(CallbackInfoReturnable<FormattedCharSequence[]> cir) {
		if (this.vulkanperf$checkedCache || this.renderMessages == null) {
			return;
		}
		this.vulkanperf$checkedCache = true;
		this.vulkanperf$shouldCache = true;
		for (FormattedCharSequence line : this.renderMessages) {
			if (!this.vulkanperf$shouldCache) {
				break;
			}
			line.accept((_, style, _) -> {
				if (style.isObfuscated()) {
					this.vulkanperf$shouldCache = false;
					return false;
				}
				return true;
			});
		}
	}

	@Inject(
		method = "getRenderMessages",
		at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/entity/SignText;renderMessages:[Lnet/minecraft/util/FormattedCharSequence;", opcode = Opcodes.PUTFIELD)
	)
	private void vulkanperf$invalidateSignCache(CallbackInfoReturnable<FormattedCharSequence[]> cir) {
		this.vulkanperf$shouldCache = false;
		this.vulkanperf$checkedCache = false;
	}

	@Override
	public boolean vulkanperf$shouldCache() {
		return this.vulkanperf$shouldCache;
	}

	@Override
	public void vulkanperf$setShouldCache(boolean shouldCache) {
		this.vulkanperf$shouldCache = shouldCache;
	}
}
