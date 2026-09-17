package dev.vulkanperf.mixin.network;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import dev.vulkanperf.config.PerfConfig;
import dev.vulkanperf.network.Varint21Prepender;
import io.netty.channel.ChannelOutboundHandler;
import net.minecraft.network.Connection;

/**
 * Every network channel allocates its own frame prepender today; the handler is stateless, so
 * one shared instance serves them all.
 */
@Mixin(Connection.class)
public abstract class ConnectionFrameEncoderMixin {

	@ModifyReturnValue(method = "createFrameEncoder", at = @At("RETURN"))
	private static ChannelOutboundHandler vp$sharePrepender(ChannelOutboundHandler original) {
		if (PerfConfig.get().network.enabled && original instanceof net.minecraft.network.Varint21LengthFieldPrepender) {
			return Varint21Prepender.INSTANCE;
		}
		return original;
	}
}
