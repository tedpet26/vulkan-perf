package dev.vulkanperf.mixin.logic.sleeping;

import dev.vulkanperf.config.PerfConfig;
import dev.vulkanperf.logic.sleeping.BlockEntitySleep;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drops sleep-tracking entries when the block entity is removed so the map
 * cannot grow unbounded across chunk loads.
 */
@Mixin(BlockEntity.class)
public abstract class BlockEntityRemovedSleepMixin {
	@Inject(method = "setRemoved", at = @At("TAIL"))
	private void vulkanperf$forget(CallbackInfo ci) {
		if (PerfConfig.get().logic.sleepingBlockEntities) {
			BlockEntity self = (BlockEntity) (Object) this;
			if (self.hasLevel()) {
				BlockEntitySleep.onRemove(self.getBlockPos().asLong());
			}
		}
	}
}
