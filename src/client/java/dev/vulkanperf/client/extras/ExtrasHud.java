package dev.vulkanperf.client.extras;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class ExtrasHud {
	private ExtrasHud() {
	}

	public static void draw(GuiGraphicsExtractor graphics) {
		Minecraft client = Minecraft.getInstance();
		if (client == null || graphics == null) {
			return;
		}
		if (client.debugEntries.isOverlayVisible() || client.gui.hud.isHidden()) {
			return;
		}
		int y = 2;
		if (ExtrasRuntime.showOverlayFps()) {
			String fps = "FPS: " + ExtrasRuntime.fps();
			if (dev.vulkanperf.config.PerfConfig.get().extras.overlayFpsExtended) {
				fps += " avg " + ExtrasRuntime.overlayAvgFps() + " low " + ExtrasRuntime.overlayLowFps();
			}
			graphics.text(client.font, fps, 2, y, 0xFFFFFFFF, true);
			y += client.font.lineHeight + 2;
		}
		if (ExtrasRuntime.overlayCoords() && client.player != null) {
			if (client.showOnlyReducedInfo()) {
				graphics.text(client.font, Component.literal("XYZ: hidden"), 2, y, 0xFFFFFFFF, true);
			} else {
				var pos = client.player.position();
				String line = String.format("XYZ: %.2f / %.2f / %.2f", pos.x, pos.y, pos.z);
				graphics.text(client.font, line, 2, y, 0xFFFFFFFF, true);
			}
		}
	}
}
