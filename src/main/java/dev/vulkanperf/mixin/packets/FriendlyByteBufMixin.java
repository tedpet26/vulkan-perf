package dev.vulkanperf.mixin.packets;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(FriendlyByteBuf.class)
public abstract class FriendlyByteBufMixin {
	@ModifyConstant(method = "readUtf()Ljava/lang/String;", constant = @Constant(intValue = 32767))
	private int vulkanperf$readUtf(int original) {
		return PerfConfig.get().packets.stringSize;
	}

	@ModifyConstant(method = "writeUtf(Ljava/lang/String;)Lnet/minecraft/network/FriendlyByteBuf;", constant = @Constant(intValue = 32767))
	private int vulkanperf$writeUtf(int original) {
		return PerfConfig.get().packets.stringSize;
	}
}
