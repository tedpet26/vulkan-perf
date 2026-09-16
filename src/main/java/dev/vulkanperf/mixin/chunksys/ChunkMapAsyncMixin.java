package dev.vulkanperf.mixin.chunksys;

import dev.vulkanperf.chunks.ChunkWorkers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.Executor;

/**
 * Accessor-style mixin documenting the ChunkMap executor wiring point. The
 * executor redirect lives in chunks.ChunkMapIoMixin; this class exists so
 * other chunksys mixins can @Shadow-derive from the same target if needed.
 */
@Mixin(net.minecraft.server.level.ChunkMap.class)
public abstract class ChunkMapAsyncMixin {
	@Redirect(method = "*", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/Executor;execute(Ljava/lang/Runnable;)V"), require = 0)
	private void vulkanperf$noopRedirect(Executor instance, Runnable command) {
		instance.execute(command);
	}

	static {
		ChunkWorkers.class.getName();
	}
}
