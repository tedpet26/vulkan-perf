package dev.vulkanperf.mixin.memory.blockstate;

import dev.vulkanperf.memory.cache.BlockStateCacheDeduper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks the two ends of {@code BlockStateBase#initCache} to deduplicate the
 * collision shape / face-sturdiness arrays it computes.
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateCacheDedupMixin {

	@Shadow
	protected abstract BlockState asState();

	@Inject(method = "initCache", at = @At("HEAD"))
	private void vulkanperf$captureOldCache(CallbackInfo ci) {
		BlockStateCacheDeduper.captureBefore(this.asState());
	}

	@Inject(method = "initCache", at = @At("TAIL"))
	private void vulkanperf$deduplicateNewCache(CallbackInfo ci) {
		BlockStateCacheDeduper.deduplicateAfter(this.asState());
	}
}
