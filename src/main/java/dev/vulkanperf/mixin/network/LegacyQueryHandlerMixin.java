package dev.vulkanperf.mixin.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.vulkanperf.config.PerfConfig;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.network.LegacyQueryHandler;

/**
 * Drops reads on channels that are already closing: the legacy-ping handler would otherwise do
 * full parse work per packet during nullping floods on dead connections.
 */
@Mixin(LegacyQueryHandler.class)
public abstract class LegacyQueryHandlerMixin {

	@Inject(method = "channelRead", at = @At("HEAD"), cancellable = true)
	private void vp$dropDeadChannelReads(ChannelHandlerContext ctx, Object msg, CallbackInfo ci) {
		if (PerfConfig.get().network.enabled && !ctx.channel().isActive()) {
			ci.cancel();
		}
	}
}
