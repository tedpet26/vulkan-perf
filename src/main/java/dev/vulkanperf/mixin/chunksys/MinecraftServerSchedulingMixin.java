package dev.vulkanperf.mixin.chunksys;

import com.llamalad7.mixinextras.sugar.Local;
import dev.vulkanperf.chunksys.MidTickChunkTasks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

/**
 * C2ME-style scheduling: mid-tick chunk task draining right after each level
 * ticks, so a heavy tick cannot starve chunk load/light callbacks. The level
 * is captured with MixinExtras {@code @Local} from the enclosing for-each loop.
 */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerSchedulingMixin {
	@Inject(
		method = "tickChildren",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;tick(Ljava/util/function/BooleanSupplier;)V", shift = At.Shift.AFTER)
	)
	private void vulkanperf$midTickChunkTasks(BooleanSupplier haveTime, CallbackInfo ci, @Local(ordinal = 0) ServerLevel level) {
		MidTickChunkTasks.runMidTick(level);
	}
}
