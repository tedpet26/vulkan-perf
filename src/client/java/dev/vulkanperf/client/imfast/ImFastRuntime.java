package dev.vulkanperf.client.imfast;

import com.mojang.renderpearl.api.device.DeviceInfo;
import com.mojang.renderpearl.api.device.GpuDevice;
import dev.vulkanperf.VulkanPerf;
import dev.vulkanperf.client.backend.BackendGuard;
import dev.vulkanperf.client.imfast.sign.SignTextCache;
import dev.vulkanperf.config.PerfConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * Runtime switches for ImmediatelyFast-class client opts. Config values are copied here so
 * GPU heuristics and resource-pack conflict handling can disable a feature without rewriting
 * {@code vulkanperf.json}.
 */
public final class ImFastRuntime {
	private static boolean fontAtlasResizing;
	private static boolean mapAtlasGeneration;
	private static boolean avoidRedundantFramebufferSwitching;
	private static boolean fixSlowBufferUploadOnAppleGpu;
	private static boolean signTextBuffering;
	private static int fontAtlasSize = 1024;
	private static int mapAtlasSize = 2048;
	private static int signAtlasSize = 4096;
	private static boolean rendererReady;
	private static SignTextCache signTextCache;
	private static boolean signReloadRegistered;

	private ImFastRuntime() {
	}

	public static void syncFromConfig() {
		PerfConfig.ImFastConfig config = PerfConfig.get().imfast;
		fontAtlasResizing = config.fontAtlasResizing;
		mapAtlasGeneration = config.mapAtlasGeneration;
		signTextBuffering = config.signTextBuffering && !FabricLoader.getInstance().isModLoaded("iris");
		fontAtlasSize = roundPowerOfTwo(config.fontAtlasSize, 256);
		mapAtlasSize = roundPowerOfTwo(config.mapAtlasSize, 2048);
		signAtlasSize = roundPowerOfTwo(config.signAtlasSize, 4096);
		if (!rendererReady) {
			avoidRedundantFramebufferSwitching = config.avoidRedundantFramebufferSwitching;
			fixSlowBufferUploadOnAppleGpu = config.fixSlowBufferUploadOnAppleGpu;
		}
		if (fontAtlasSize != config.fontAtlasSize) {
			VulkanPerf.LOGGER.warn("imfast fontAtlasSize {} is not a power of two; using {}", config.fontAtlasSize, fontAtlasSize);
			config.fontAtlasSize = fontAtlasSize;
		}
		if (mapAtlasSize != config.mapAtlasSize) {
			VulkanPerf.LOGGER.warn("imfast mapAtlasSize {} is not a power of two; using {}", config.mapAtlasSize, mapAtlasSize);
			config.mapAtlasSize = mapAtlasSize;
		}
		if (signAtlasSize != config.signAtlasSize) {
			VulkanPerf.LOGGER.warn("imfast signAtlasSize {} is not a power of two; using {}", config.signAtlasSize, signAtlasSize);
			config.signAtlasSize = signAtlasSize;
		}
	}

	public static void onRenderSystemInit(GpuDevice device) {
		syncFromConfig();
		DeviceInfo info = device.getDeviceInfo();
		GpuHeuristics.Result heuristics = GpuHeuristics.evaluate(info, BackendGuard.isVulkan());
		avoidRedundantFramebufferSwitching &= heuristics.avoidRedundantFramebufferSwitching();
		fixSlowBufferUploadOnAppleGpu &= heuristics.fixSlowBufferUploadOnAppleGpu();
		rendererReady = true;
		VulkanPerf.LOGGER.info(
			"imfast ready backend={} vendor={} device={} atlasFont={} atlasMap={} glFbSkip={} appleUpload={} signs={}",
			info.backendName(),
			info.vendorName(),
			info.name(),
			fontAtlasSize,
			mapAtlasSize,
			avoidRedundantFramebufferSwitching,
			fixSlowBufferUploadOnAppleGpu,
			signTextBuffering
		);
	}

	public static void lateInit() {
		if (!PerfConfig.get().imfast.enabled) {
			return;
		}
		if (signTextBuffering) {
			signTextCache = new SignTextCache();
			registerSignReloadListener();
		}
	}

	public static void onLevelChange() {
		if (signTextCache != null) {
			signTextCache.clear();
		}
	}

	public static boolean enabled() {
		return PerfConfig.get().imfast.enabled;
	}

	public static boolean fontAtlasResizing() {
		return enabled() && fontAtlasResizing;
	}

	public static void setFontAtlasResizing(boolean value) {
		fontAtlasResizing = value && PerfConfig.get().imfast.fontAtlasResizing;
	}

	public static boolean mapAtlasGeneration() {
		return enabled() && mapAtlasGeneration;
	}

	public static void setMapAtlasGeneration(boolean value) {
		mapAtlasGeneration = value && PerfConfig.get().imfast.mapAtlasGeneration;
	}

	public static boolean avoidRedundantFramebufferSwitching() {
		return enabled() && avoidRedundantFramebufferSwitching && !BackendGuard.isVulkan();
	}

	public static boolean fixSlowBufferUploadOnAppleGpu() {
		return enabled() && fixSlowBufferUploadOnAppleGpu && !BackendGuard.isVulkan();
	}

	public static boolean signTextBuffering() {
		return enabled() && signTextBuffering && signTextCache != null;
	}

	public static boolean enhancedBatching() {
		return enabled() && PerfConfig.get().imfast.enhancedBatching;
	}

	public static boolean fastTextLookup() {
		return enabled() && PerfConfig.get().imfast.fastTextLookup;
	}

	public static boolean skipTextTranslucencySorting() {
		return enabled() && PerfConfig.get().imfast.skipTextTranslucencySorting;
	}

	public static boolean batchAnimatedItemUpdates() {
		return enabled() && PerfConfig.get().imfast.batchAnimatedItemUpdates;
	}

	public static int fontAtlasSize() {
		return fontAtlasSize;
	}

	public static int mapAtlasSize() {
		return mapAtlasSize;
	}

	public static int signAtlasSize() {
		return signAtlasSize;
	}

	public static boolean rendererReady() {
		return rendererReady;
	}

	public static SignTextCache signTextCache() {
		return signTextCache;
	}

	public static int roundPowerOfTwo(int value, int fallback) {
		if (value <= 0) {
			return fallback;
		}
		if (isPowerOfTwo(value)) {
			return value;
		}
		int rounded = 1 << (Integer.SIZE - Integer.numberOfLeadingZeros(value - 1));
		return Math.max(rounded, fallback);
	}

	public static boolean isPowerOfTwo(int value) {
		return value > 0 && (value & (value - 1)) == 0;
	}

	private static void registerSignReloadListener() {
		if (signReloadRegistered || signTextCache == null) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		if (client == null) {
			return;
		}
		ResourceManager manager = client.getResourceManager();
		if (manager instanceof ReloadableResourceManager reloadable) {
			reloadable.registerReloadListener(signTextCache);
			signReloadRegistered = true;
		}
	}
}
