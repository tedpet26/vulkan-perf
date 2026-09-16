package dev.vulkanperf.mixin.memory.accessors;

import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.SubShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SubShape.class)
public interface SubShapeAccessor extends DiscreteVoxelShapeAccessor {
	@Accessor("parent")
	DiscreteVoxelShape vulkanperf$getParent();

	@Accessor("startX")
	int vulkanperf$getStartX();

	@Accessor("startY")
	int vulkanperf$getStartY();

	@Accessor("startZ")
	int vulkanperf$getStartZ();

	@Accessor("endX")
	int vulkanperf$getEndX();

	@Accessor("endY")
	int vulkanperf$getEndY();

	@Accessor("endZ")
	int vulkanperf$getEndZ();
}
