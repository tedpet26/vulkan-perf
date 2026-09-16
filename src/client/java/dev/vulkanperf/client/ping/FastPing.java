package dev.vulkanperf.client.ping;

import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Optional;

public final class FastPing {
	private FastPing() {
	}

	public static Optional<ResolvedServerAddress> resolve(ServerNameResolver vanilla, ServerAddress address) {
		if (!NumericAddress.isNumeric(address.getHost())) {
			return vanilla.resolveAddress(address);
		}
		try {
			return Optional.of(ResolvedServerAddress.from(
				new InetSocketAddress(NumericAddress.withoutReverseLookup(address.getHost()), address.getPort())
			));
		} catch (UnknownHostException e) {
			return Optional.empty();
		}
	}
}
