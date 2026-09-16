package dev.vulkanperf.mixin.memory.accessors;

import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DiscreteVoxelShape.class)
public interface DiscreteVoxelShapeAccessor {
	@Accessor("xSize")
	int vulkanperf$getXSize();

	@Accessor("ySize")
	int vulkanperf$getYSize();

	@Accessor("zSize")
	int vulkanperf$getZSize();
}
