package dev.vulkanperf.mixin.logic.chunk;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skips random-tick iteration for sections that contain nothing tickable.
 */
@Mixin(LevelChunkSection.class)
public abstract class LevelChunkSectionRandomTickMixin {
	@Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$skipEmptySection(CallbackInfo ci) {
		if (!PerfConfig.get().logic.randomTickSkip) {
			return;
		}
		LevelChunkSection self = (LevelChunkSection) (Object) this;
		if (self.hasOnlyAir()) {
			ci.cancel();
		}
	}
}
