package dev.vulkanperf.mixin.logic.sleeping;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.vulkanperf.logic.sleeping.SleepingBlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Registers the rebindable ticker wrapper on each block entity so it can park/restore its own
 * ticker. Runs after every ticker (re)creation; a fresh ticker always clears sleep state first.
 */
@Mixin(LevelChunk.class)
public abstract class LevelChunkTickerHookMixin {

	@Inject(method = "updateBlockEntityTicker", at = @At("TAIL"))
	private <T extends BlockEntity> void vp$attachWrapper(T blockEntity, CallbackInfo ci) {
		if (!(blockEntity instanceof SleepingBlockEntity sleeper)) {
			return;
		}
		TickingBlockEntity wrapper = ((LevelChunkAccessor) this).vp$getTickersInLevel().get(blockEntity.getBlockPos());
		sleeper.vp$setTickWrapper(wrapper instanceof SleepingBlockEntity.TickerWrapper tickWrapper ? tickWrapper : null);
	}
}
