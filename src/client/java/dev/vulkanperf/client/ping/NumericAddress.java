package dev.vulkanperf.client.ping;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class NumericAddress {
	private NumericAddress() {
	}

	public static InetAddress withoutReverseLookup(String host) throws UnknownHostException {
		InetAddress resolved = InetAddress.getByName(host);
		if (isNumeric(host)) {
			return InetAddress.getByAddress(resolved.getHostAddress(), resolved.getAddress());
		}
		return resolved;
	}

	public static boolean isNumeric(String host) {
		if (host == null || host.isEmpty()) {
			return false;
		}
		if (host.indexOf(':') >= 0) {
			return host.chars().allMatch(c -> Character.digit(c, 16) >= 0 || c == ':' || c == '.' || c == '%');
		}
		String[] parts = host.split("\\.");
		if (parts.length != 4) {
			return false;
		}
		for (String part : parts) {
			if (part.isEmpty() || part.length() > 3) {
				return false;
			}
			int value = 0;
			for (int i = 0; i < part.length(); i++) {
				char c = part.charAt(i);
				if (c < '0' || c > '9') {
					return false;
				}
				value = value * 10 + (c - '0');
			}
			if (value > 255) {
				return false;
			}
		}
		return true;
	}
}
