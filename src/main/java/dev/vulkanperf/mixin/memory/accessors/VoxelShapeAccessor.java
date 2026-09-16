package dev.vulkanperf.mixin.memory.accessors;

import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VoxelShape.class)
public interface VoxelShapeAccessor {
	@Accessor("shape")
	DiscreteVoxelShape vulkanperf$getShape();

	@Accessor("shape")
	@Mutable
	void vulkanperf$setShape(DiscreteVoxelShape shape);

	@Accessor("faces")
	@Nullable
	VoxelShape[] vulkanperf$getFaces();

	@Accessor("faces")
	void vulkanperf$setFaces(@Nullable VoxelShape[] faces);
}
