package dev.vulkanperf.mixin.memory.accessors;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.world.phys.shapes.ArrayVoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ArrayVoxelShape.class)
public interface ArrayVoxelShapeAccessor extends VoxelShapeAccessor {
	@Accessor("xs")
	DoubleList vulkanperf$getXPoints();

	@Accessor("xs")
	@Mutable
	void vulkanperf$setXPoints(DoubleList points);

	@Accessor("ys")
	DoubleList vulkanperf$getYPoints();

	@Accessor("ys")
	@Mutable
	void vulkanperf$setYPoints(DoubleList points);

	@Accessor("zs")
	DoubleList vulkanperf$getZPoints();

	@Accessor("zs")
	@Mutable
	void vulkanperf$setZPoints(DoubleList points);
}
