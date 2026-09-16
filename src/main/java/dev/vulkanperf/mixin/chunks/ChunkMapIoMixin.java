package dev.vulkanperf.mixin.chunks;

import com.llamalad7.mixinextras.sugar.Local;
import dev.vulkanperf.chunks.ChunkWorkers;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ChunkMap.class)
public abstract class ChunkMapIoMixin {
	@Redirect(method = "save(Lnet/minecraft/world/level/chunk/ChunkAccess;)Z", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
	private <T> CompletableFuture<T> vulkanperf$chunkIo(Supplier<T> supplier, Executor executor, @Local(argsOnly = true) ChunkAccess chunk) {
		long pos = chunk.getPos().pack();
		return CompletableFuture.supplyAsync(ChunkWorkers.lockedSave(pos, supplier), ChunkWorkers.serialize(executor));
	}
}
