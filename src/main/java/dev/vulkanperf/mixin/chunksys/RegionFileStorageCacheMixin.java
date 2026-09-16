package dev.vulkanperf.mixin.chunksys;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bounds the region-file cache (C2ME opts-chunkio idea). Vanilla's
 * MAX_CACHE_SIZE is 256; we only log when configured larger.
 */
@Mixin(RegionFileStorage.class)
public abstract class RegionFileStorageCacheMixin {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void vulkanperf$configureCache(net.minecraft.world.level.chunk.storage.RegionStorageInfo info, java.nio.file.Path folder, boolean sync, CallbackInfo ci) {
		int configured = PerfConfig.get().chunks.regionFileCacheSize;
		if (configured > 256) {
			dev.vulkanperf.VulkanPerf.LOGGER.info("region file cache: {} (vanilla 256)", configured);
		}
	}
}
