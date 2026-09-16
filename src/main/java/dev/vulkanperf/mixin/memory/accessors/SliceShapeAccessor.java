package dev.vulkanperf.mixin.memory.accessors;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.SliceShape;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SliceShape.class)
public interface SliceShapeAccessor extends VoxelShapeAccessor {
	@Accessor("delegate")
	VoxelShape vulkanperf$getDelegate();

	@Accessor("axis")
	Direction.Axis vulkanperf$getAxis();
}
