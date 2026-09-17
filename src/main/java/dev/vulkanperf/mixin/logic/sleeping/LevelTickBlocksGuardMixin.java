package dev.vulkanperf.mixin.logic.sleeping;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Sleeping tickers report a null position (they have no work tied to chunk ticking), so the
 * vanilla {@code shouldTickBlocksAt(pos)} guard would NPE on them. Null positions are ticked
 * unconditionally — the sleeping ticker itself is a no-op — while every real ticker keeps the
 * vanilla chunk-ticking gate.
 */
@Mixin(Level.class)
public abstract class LevelTickBlocksGuardMixin {

	@Redirect(
		method = "tickBlockEntities",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;shouldTickBlocksAt(Lnet/minecraft/core/BlockPos;)Z")
	)
	private static boolean vp$nullPosSafeShouldTick(Level level, BlockPos pos) {
		return pos == null || level.shouldTickBlocksAt(pos);
	}
}
