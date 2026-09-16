package dev.vulkanperf.client.batching;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.vulkanperf.config.PerfConfig;

public final class HudBatcher {
	private static boolean active;
	private static int passes;

	private HudBatcher() {
	}

	public static void beginPass() {
		if (!PerfConfig.get().batching.enabled) {
			return;
		}
		active = RenderSystem.tryGetDevice() != null;
		if (active) {
			passes++;
		}
	}

	public static void endPass() {
		active = false;
	}

	public static boolean deferring() {
		return false;
	}

	public static int passes() {
		return passes;
	}

	public static void tick() {
	}
}
