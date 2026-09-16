package dev.vulkanperf.client.mixin.ping;

import dev.vulkanperf.client.ping.FastPing;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(ServerStatusPinger.class)
public abstract class ServerStatusPingerMixin {
	@Redirect(method = "pingServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/resolver/ServerNameResolver;resolveAddress(Lnet/minecraft/client/multiplayer/resolver/ServerAddress;)Ljava/util/Optional;"))
	private Optional<ResolvedServerAddress> vulkanperf$fastResolve(ServerNameResolver resolver, ServerAddress address) {
		return FastPing.resolve(resolver, address);
	}
}
