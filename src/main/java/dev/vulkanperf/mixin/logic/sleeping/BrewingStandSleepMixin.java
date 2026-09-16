package dev.vulkanperf.mixin.logic.sleeping;

import dev.vulkanperf.config.PerfConfig;
import dev.vulkanperf.logic.sleeping.BlockEntitySleep;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandSleepMixin {
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
