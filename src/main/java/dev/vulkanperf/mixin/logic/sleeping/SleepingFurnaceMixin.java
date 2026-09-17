package dev.vulkanperf.mixin.logic.sleeping;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.vulkanperf.logic.sleeping.SleepingBlockEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

/**
 * An idle furnace (not lit, nothing cooking, and no fuel+ingredient pair that could start) has
 * nothing to do until its inventory or block state changes; both wake it.
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class SleepingFurnaceMixin {

	@Shadow
	private int litTimeRemaining;

	@Shadow
	private int cookingTimer;

	@Shadow
	private NonNullList<ItemStack> items;

	@Inject(method = "serverTick", at = @At("RETURN"))
	private static void vulkanperf$maybeSleep(
		ServerLevel level, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state,
		AbstractFurnaceBlockEntity entity, CallbackInfo ci
	) {
		SleepingFurnaceMixin self = (SleepingFurnaceMixin) (Object) entity;
		boolean hasIngredient = !self.items.get(0).isEmpty();
		boolean hasFuel = !self.items.get(1).isEmpty();
		if (self.litTimeRemaining == 0 && self.cookingTimer == 0 && !(hasFuel && hasIngredient)) {
			((SleepingBlockEntity) entity).vp$startSleeping();
		}
	}

	@Inject(method = "loadAdditional", at = @At("RETURN"))
	private void vulkanperf$wakeOnLoad(net.minecraft.world.level.storage.ValueInput input, CallbackInfo ci) {
		((SleepingBlockEntity) this).vp$wakeUpNow();
	}
}
