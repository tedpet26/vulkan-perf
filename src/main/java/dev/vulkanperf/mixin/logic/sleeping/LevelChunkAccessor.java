package dev.vulkanperf.mixin.logic.sleeping;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

@Mixin(LevelChunk.class)
public interface LevelChunkAccessor {
	@Accessor("tickersInLevel")
	Map<BlockPos, TickingBlockEntity> vp$getTickersInLevel();
}
