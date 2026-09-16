package dev.vulkanperf.mixin.logic.chunk;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fast negative answer for sections with nothing tickable: 26.3 tracks
 * tickable counts incrementally, so make the combined query cheap to reuse.
 * (26.3 has no per-section randomTick method; the flag is consumed by
 * ServerLevel's ticking loop.)
 */
@Mixin(LevelChunkSection.class)
public abstract class LevelChunkSectionRandomTickMixin {
	@Inject(method = "isRandomlyTicking", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$skipEmptySection(CallbackInfoReturnable<Boolean> cir) {
		if (!PerfConfig.get().logic.randomTickSkip) {
			return;
		}
		LevelChunkSection self = (LevelChunkSection) (Object) this;
		if (self.hasOnlyAir()) {
			cir.setReturnValue(false);
		}
	}
}
