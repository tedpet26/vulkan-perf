package dev.vulkanperf.mixin.logic.sleeping;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.vulkanperf.logic.sleeping.SleepingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Campfires tick only for cooking progress: a lit campfire with empty slots and an unlit one
 * with no progress to decay have nothing to do. Food placement and NBT edits wake them via
 * {@code setChanged} / load.
 */
@Mixin(CampfireBlockEntity.class)
public abstract class SleepingCampfireMixin {

	@Shadow
	private NonNullList<ItemStack> items;

	@Shadow
	private int[] cookingProgress;

	@Inject(method = "cookTick", at = @At("RETURN"))
	private static void vulkanperf$sleepWhenEmpty(
		ServerLevel level, BlockPos pos, BlockState state, CampfireBlockEntity entity,
		net.minecraft.world.item.crafting.RecipeManager.CachedCheck<net.minecraft.world.item.crafting.SingleRecipeInput, net.minecraft.world.item.crafting.CampfireCookingRecipe> recipeCache,
		CallbackInfo ci
	) {
		SleepingCampfireMixin self = (SleepingCampfireMixin) (Object) entity;
		for (int slot = 0; slot < self.items.size(); slot++) {
			if (!self.items.get(slot).isEmpty()) {
				return;
			}
		}
		((SleepingBlockEntity) entity).vp$startSleeping();
	}

	@Inject(method = "cooldownTick", at = @At("RETURN"))
	private static void vulkanperf$sleepWhenNoProgress(
		Level level, BlockPos pos, BlockState state, CampfireBlockEntity entity, CallbackInfo ci
	) {
		SleepingCampfireMixin self = (SleepingCampfireMixin) (Object) entity;
		for (int progress : self.cookingProgress) {
			if (progress > 0) {
				return;
			}
		}
		((SleepingBlockEntity) entity).vp$startSleeping();
	}

}
