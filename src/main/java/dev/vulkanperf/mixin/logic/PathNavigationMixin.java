package dev.vulkanperf.mixin.logic;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PathNavigation.class)
public abstract class PathNavigationMixin {
	@Unique
	private BlockPos vulkanperf$lastTarget;
	@Unique
	private long vulkanperf$missUntil;

	@Inject(method = "createPath(Lnet/minecraft/core/BlockPos;I)Lnet/minecraft/world/level/pathfinder/Path;", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$skipKnownMiss(BlockPos target, int distance, CallbackInfoReturnable<Path> cir) {
		if (!PerfConfig.get().logic.pathCache || target == null) {
			return;
		}
		if (target.equals(vulkanperf$lastTarget) && System.nanoTime() < vulkanperf$missUntil) {
			cir.setReturnValue(null);
		}
	}

	@Inject(method = "createPath(Lnet/minecraft/core/BlockPos;I)Lnet/minecraft/world/level/pathfinder/Path;", at = @At("RETURN"))
	private void vulkanperf$rememberMiss(BlockPos target, int distance, CallbackInfoReturnable<Path> cir) {
		if (!PerfConfig.get().logic.pathCache || target == null || cir.getReturnValue() != null) {
			return;
		}
		vulkanperf$lastTarget = target.immutable();
		vulkanperf$missUntil = System.nanoTime() + 50_000_000L;
	}
}
