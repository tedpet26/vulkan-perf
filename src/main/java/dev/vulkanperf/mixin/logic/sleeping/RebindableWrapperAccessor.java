package dev.vulkanperf.mixin.logic.sleeping;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import dev.vulkanperf.logic.sleeping.SleepingBlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;

/**
 * Exposes {@code LevelChunk.RebindableTickingBlockEntityWrapper} (private nested class) as the
 * {@link SleepingBlockEntity.TickerWrapper} duck so the sleeping system can rebind tickers.
 */
@Mixin(targets = "net.minecraft.world.level.chunk.LevelChunk$RebindableTickingBlockEntityWrapper")
public interface RebindableWrapperAccessor extends SleepingBlockEntity.TickerWrapper {

	@Override
	@Accessor("ticker")
	TickingBlockEntity vp$getWrapped();

	@Override
	@Invoker("rebind")
	void vp$rebindWrapped(TickingBlockEntity ticker);
}
