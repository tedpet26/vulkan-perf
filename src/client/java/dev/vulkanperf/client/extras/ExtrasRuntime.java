package dev.vulkanperf.client.extras;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class ExtrasRuntime {
	private static long lastOverlayMs;
	private static int cachedFps;
	private static int fpsMin = Integer.MAX_VALUE;
	private static int fpsAccum;
	private static int fpsSamples;

	private ExtrasRuntime() {
	}

	public static void tick() {
		if (!PerfConfig.get().extras.enabled) {
			return;
		}
		long now = System.currentTimeMillis();
		if (now - lastOverlayMs >= Math.max(50, PerfConfig.get().extras.overlayUpdateMs)) {
			int fps = Minecraft.getInstance().getFps();
			cachedFps = fps;
			fpsMin = Math.min(fpsMin, fps);
			fpsAccum += fps;
			fpsSamples++;
			lastOverlayMs = now;
		}
	}

	public static int fps() {
		return cachedFps;
	}

	public static int overlayAvgFps() {
		return fpsSamples == 0 ? cachedFps : fpsAccum / fpsSamples;
	}

	public static int overlayLowFps() {
		return fpsMin == Integer.MAX_VALUE ? cachedFps : fpsMin;
	}

	public static boolean fog() {
		return extras().fog;
	}

	public static boolean animations() {
		return extras().animation;
	}

	public static boolean animate(Identifier sprite) {
		PerfConfig.ExtrasConfig extras = extras();
		if (!extras.animation) {
			return false;
		}
		if (sprite == null) {
			return extras.animation;
		}
		String path = sprite.getPath();
		if (path.contains("water")) {
			return extras.animateWater;
		}
		if (path.contains("lava")) {
			return extras.animateLava;
		}
		if (path.contains("fire") || path.contains("campfire")) {
			return extras.animateFire;
		}
		if (path.contains("nether_portal") || path.contains("portal")) {
			return extras.animatePortal;
		}
		if (path.contains("sculk") || path.contains("vibration")) {
			return extras.animateSculk;
		}
		return extras.animateBlocks;
	}

	public static boolean particles() {
		return extras().particles;
	}

	public static boolean weatherParticles() {
		return extras().particles && extras().weatherParticles;
	}

	public static boolean rainSplash() {
		return extras().particles && extras().rainSplash;
	}

	public static boolean blockBreak() {
		return extras().particles && extras().blockBreak;
	}

	public static boolean blockBreaking() {
		return extras().particles && extras().blockBreaking;
	}

	public static boolean sky() {
		return extras().sky;
	}

	public static boolean sun() {
		return extras().sun;
	}

	public static boolean moon() {
		return extras().moon;
	}

	public static boolean stars() {
		return extras().stars;
	}

	public static boolean weather() {
		return extras().rainSnow;
	}

	public static boolean beacons() {
		return extras().beaconBeams;
	}

	public static boolean nametags() {
		return extras().nametags;
	}

	public static boolean itemFrames() {
		return extras().itemFrames;
	}

	public static boolean armorStands() {
		return extras().armorStands;
	}

	public static boolean paintings() {
		return extras().paintings;
	}

	public static boolean pistons() {
		return extras().pistons;
	}

	public static boolean enchantingTableBook() {
		return extras().enchantingTableBook;
	}

	public static boolean lightUpdates() {
		return extras().lightUpdates;
	}

	public static boolean toasts() {
		return extras().toasts;
	}

	public static boolean showOverlayFps() {
		return extras().overlayFps;
	}

	public static boolean overlayCoords() {
		return extras().overlayCoords;
	}

	public static boolean biomeColors() {
		return extras().biomeColors;
	}

	public static boolean skyColors() {
		return extras().skyColors;
	}

	public static boolean itemFrameNametags() {
		return extras().itemFrameNametags;
	}

	private static PerfConfig.ExtrasConfig extras() {
		return PerfConfig.get().extras;
	}
}
