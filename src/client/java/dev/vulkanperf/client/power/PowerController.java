package dev.vulkanperf.client.power;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.client.Minecraft;

public final class PowerController {
	private PowerController() {
	}

	public static void tick(Minecraft client) {
	}

	public static int frameCap(Minecraft client, int vanilla) {
		if (!PerfConfig.get().power.enabled) {
			return vanilla;
		}
		if (isHidden(client)) {
			return Math.min(vanilla, PerfConfig.get().power.hiddenFps);
		}
		if (!client.isWindowActive()) {
			return Math.min(vanilla, PerfConfig.get().power.unfocusedFps);
		}
		return vanilla;
	}

	public static boolean shouldMute(Minecraft client) {
		return PerfConfig.get().power.enabled
			&& PerfConfig.get().power.muteUnfocused
			&& client != null
			&& !client.isWindowActive();
	}

	public static boolean shouldSkipTick(Minecraft client) {
		return PerfConfig.get().power.enabled && isHidden(client);
	}

	public static boolean isHidden(Minecraft client) {
		if (client == null || client.getWindow() == null) {
			return false;
		}
		return client.getWindow().getWidth() <= 0 || client.getWindow().getHeight() <= 0;
	}
}
