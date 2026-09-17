package dev.vulkanperf.mixin.logic.hopper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.vulkanperf.config.PerfConfig;
import dev.vulkanperf.logic.hopper.ContainerChangeBus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block-level wake sources for sleeping hoppers: any neighbour change (redstone ENABLED flag,
 * replaced containers, composter states), placement, and removal. Removal also drops the
 * hopper's watcher registrations so the bus does not accumulate stale entries.
 */
@Mixin(HopperBlock.class)
public abstract class HopperBlockWakeMixin {

	@Inject(method = "neighborChanged", at = @At("HEAD"))
	private void vulkanperf$wakeOnNeighbor(
		BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block,
		net.minecraft.world.level.redstone.Orientation orientation, boolean movedByPiston, CallbackInfo ci
	) {
		vulkanperf$wake(level, pos);
	}

	@Inject(method = "onPlace", at = @At("HEAD"))
	private void vulkanperf$wakeOnPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston, CallbackInfo ci) {
		vulkanperf$wake(level, pos);
	}

	@Inject(method = "affectNeighborsAfterRemoval", at = @At("HEAD"))
	private void vulkanperf$unwatchOnRemove(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, boolean movedByPiston, CallbackInfo ci) {
		if (PerfConfig.get().logic.hopper) {
			ContainerChangeBus.unwatchAll(level, pos);
		}
	}

	private static void vulkanperf$wake(Level level, BlockPos pos) {
		if (PerfConfig.get().logic.hopper) {
			ContainerChangeBus.onBlockUpdate(level, pos);
		}
	}
}
