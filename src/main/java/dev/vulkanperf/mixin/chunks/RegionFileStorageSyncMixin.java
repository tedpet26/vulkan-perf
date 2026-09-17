package dev.vulkanperf.mixin.chunks;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;

/**
 * Region files are fsync'd on every close by default; on server-grade storage the OS writeback
 * already protects against crash windows and the per-chunk sync stalls the IO worker.
 * Opt-out via {@code -Dvulkanperf.regionSync=true}.
 */
@Mixin(RegionFileStorage.class)
public abstract class RegionFileStorageSyncMixin {
	@Mutable
	@Shadow
	@Final
	private boolean sync;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void vp$relaxSync(net.minecraft.world.level.chunk.storage.RegionStorageInfo info, java.nio.file.Path folder, boolean syncRequested, CallbackInfo ci) {
		if (!Boolean.getBoolean("vulkanperf.regionSync") && PerfConfig.get().chunks.asyncIoDeepened) {
			this.sync = false;
		}
	}
}
