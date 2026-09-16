package dev.vulkanperf.mixin.chunksys;

import dev.vulkanperf.chunksys.EnhancedAutosave;
import dev.vulkanperf.chunksys.MidTickChunkTasks;
import dev.vulkanperf.config.PerfConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

/**
 * C2ME-style scheduling: mid-tick chunk task draining after each level tick,
 * plus autosave counter handling (staggered drain keeps the vanilla
 * save-everything pass from spiking a single tick).
 */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerSchedulingMixin {
	@Shadow
	private int ticksUntilAutosave;

	@Inject(method = "tickChildren", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;tick(Ljava/util/function/BooleanSupplier;)V", shift = At.Shift.AFTER))
	private void vulkanperf$midTickChunkTasks(BooleanSupplier haveTime, CallbackInfo ci, ServerLevel level) {
		MidTickChunkTasks.runMidTick(level);
		if (PerfConfig.get().chunks.enhancedAutosave) {
			EnhancedAutosave.drainOne();
		}
	}

	@Inject(method = "autoSave", at = @At("HEAD"))
	private void vulkanperf$onAutoSave(CallbackInfo ci) {
		if (PerfConfig.get().chunks.enabled && PerfConfig.get().chunks.enhancedAutosave) {
			EnhancedAutosave.beginStaggeredSave();
		}
	}
}
