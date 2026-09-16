package dev.vulkanperf.client.hudspread;

import dev.vulkanperf.config.PerfConfig;

public final class HudSpreader {
	private static int frame;

	private HudSpreader() {
	}

	public static void tick() {
		frame++;
	}

	public static boolean shouldExtractHeavyHud() {
		int spread = Math.max(1, PerfConfig.get().hudspread.spreadFrames);
		return !PerfConfig.get().hudspread.enabled || (frame % spread) == 0;
	}
}
