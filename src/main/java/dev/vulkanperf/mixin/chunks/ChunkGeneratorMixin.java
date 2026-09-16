package dev.vulkanperf.mixin.chunks;

import dev.vulkanperf.chunks.ChunkWorkers;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.concurrent.Executor;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {
	@ModifyArg(method = "createBiomes", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
	private Executor vulkanperf$biomes(Executor original) {
		return ChunkWorkers.worldgen(original);
	}
}
