package dev.vulkanperf.mixin.packets;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.network.CompressionDecoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(CompressionDecoder.class)
public abstract class CompressionDecoderMixin {
	@ModifyConstant(method = "decode", constant = @Constant(intValue = 8388608))
	private int vulkanperf$inflateLimit(int original) {
		return PerfConfig.get().packets.compression;
	}
}
