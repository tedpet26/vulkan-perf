package dev.vulkanperf.mixin.logic.sleeping;

import dev.vulkanperf.config.PerfConfig;
import dev.vulkanperf.logic.sleeping.BlockEntitySleep;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Wakes sleeping block entities on any state change. Hooks the base
 * {@link BlockEntity#setChanged} once instead of per-subclass (furnace and
 * brewing stand both route through it in 26.3).
 */
@Mixin(BlockEntity.class)
public abstract class BlockEntitySetChangedSleepMixin {
	@Inject(method = "setChanged", at = @At("HEAD"))
	private void vulkanperf$wake(CallbackInfo ci) {
		if (PerfConfig.get().logic.sleepingBlockEntities) {
			BlockEntity self = (BlockEntity) (Object) this;
			if (self.hasLevel()) {
				BlockEntitySleep.wake(self.getBlockPos().asLong());
			}
		}
	}
}
