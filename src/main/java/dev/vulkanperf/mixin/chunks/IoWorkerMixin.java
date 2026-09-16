package dev.vulkanperf.mixin.chunks;

import dev.vulkanperf.chunks.ChunkWorkers;
import net.minecraft.world.level.chunk.storage.IOWorker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.concurrent.Executor;

@Mixin(IOWorker.class)
public abstract class IoWorkerMixin {
	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/thread/PriorityConsecutiveExecutor;<init>(ILjava/util/concurrent/Executor;Ljava/lang/String;)V"))
	private Executor vulkanperf$ioPool(Executor original) {
		return ChunkWorkers.io(original);
	}
}
