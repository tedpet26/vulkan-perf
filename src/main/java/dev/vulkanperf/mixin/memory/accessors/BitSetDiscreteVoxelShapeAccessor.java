package dev.vulkanperf.mixin.memory.accessors;

import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.BitSet;

@Mixin(BitSetDiscreteVoxelShape.class)
public interface BitSetDiscreteVoxelShapeAccessor extends DiscreteVoxelShapeAccessor {
	@Accessor("storage")
	BitSet vulkanperf$getStorage();

	@Accessor("xMin")
	int vulkanperf$getXMin();

	@Accessor("yMin")
	int vulkanperf$getYMin();

	@Accessor("zMin")
	int vulkanperf$getZMin();

	@Accessor("xMax")
	int vulkanperf$getXMax();

	@Accessor("yMax")
	int vulkanperf$getYMax();

	@Accessor("zMax")
	int vulkanperf$getZMax();
}
