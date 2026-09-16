package dev.vulkanperf.memory.cache;

import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Duck interface implemented by a mixin targeting the private
 * {@code BlockBehaviour.BlockStateBase.Cache} inner class. That class is not
 * accessible by name outside {@code BlockBehaviour}'s own compilation unit,
 * so this public view (obtained reflectively, see {@code BlockStateCacheDeduper})
 * is how we reach into it to deduplicate its shape/array fields in place.
 */
public interface ShapeCacheView {
	VoxelShape vulkanperf$getCollisionShape();

	void vulkanperf$setCollisionShape(VoxelShape shape);

	boolean @Nullable [] vulkanperf$getFaceSturdy();

	void vulkanperf$setFaceSturdy(boolean[] faceSturdy);
}
