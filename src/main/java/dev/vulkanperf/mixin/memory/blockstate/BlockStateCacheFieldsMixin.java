package dev.vulkanperf.mixin.memory.blockstate;

import dev.vulkanperf.memory.cache.ShapeCacheView;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Targets the private {@code BlockBehaviour$BlockStateBase$Cache} class by
 * string name (it cannot be referenced by type from outside {@code BlockBehaviour}),
 * exposing its shape/array fields for in-place deduplication.
 */
@Mixin(targets = "net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase$Cache")
public abstract class BlockStateCacheFieldsMixin implements ShapeCacheView {

	@Shadow
	@Final
	@Mutable
	protected VoxelShape collisionShape;

	@Shadow
	@Final
	@Mutable
	private boolean[] faceSturdy;

	@Override
	public VoxelShape vulkanperf$getCollisionShape() {
		return this.collisionShape;
	}

	@Override
	public void vulkanperf$setCollisionShape(VoxelShape shape) {
		this.collisionShape = shape;
	}

	@Override
	public boolean[] vulkanperf$getFaceSturdy() {
		return this.faceSturdy;
	}

	@Override
	public void vulkanperf$setFaceSturdy(boolean[] faceSturdy) {
		this.faceSturdy = faceSturdy;
	}
}
