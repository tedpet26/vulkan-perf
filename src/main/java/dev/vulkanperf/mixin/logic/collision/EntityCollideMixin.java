package dev.vulkanperf.mixin.logic.collision;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.vulkanperf.config.PerfConfig;
import dev.vulkanperf.logic.entity.EntityCollisions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Entity movement collision: the collider list handed to the per-axis sweep is filled lazily from
 * the block-collision iterator instead of being materialised up front, so a movement that is
 * stopped by the first shape never allocates the rest.
 */
@Mixin(Entity.class)
public abstract class EntityCollideMixin {

	@Redirect(
		method = "collide",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;collectCollidersIgnoringWorldBorder(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/Level;Ljava/util/List;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"
		)
	)
	private static List<VoxelShape> vp$collectStepUpColliders(
		Entity source, Level level, List<VoxelShape> entityColliders, AABB box
	) {
		if (PerfConfig.get().logic.entityFastMovement) {
			return EntityCollisions.collectCollidersLazy(source, level, entityColliders, box);
		}
		return EntityCollisions.collectVanilla(source, level, entityColliders, box);
	}

	@Overwrite
	public static Vec3 collideBoundingBox(
		final @Nullable Entity source, final Vec3 movement, final AABB boundingBox, final Level level, final List<VoxelShape> entityColliders
	) {
		List<VoxelShape> colliders;
		if (PerfConfig.get().logic.entityFastMovement) {
			colliders = EntityCollisions.collectCollidersLazy(source, level, entityColliders, boundingBox.expandTowards(movement));
		} else {
			colliders = EntityCollisions.collectVanilla(source, level, entityColliders, boundingBox.expandTowards(movement));
		}
		return EntityCollisions.collideWithShapes(movement, boundingBox, colliders);
	}

	@Overwrite
	public static Vec3 collideBoundingBox(
		final CollisionContext source, final Vec3 movement, final AABB boundingBox, final Level level, final List<VoxelShape> entityColliders
	) {
		List<VoxelShape> colliders;
		if (PerfConfig.get().logic.entityFastMovement) {
			colliders = EntityCollisions.collectCollidersLazy(source, level, entityColliders, boundingBox.expandTowards(movement));
		} else {
			colliders = EntityCollisions.collectVanilla(source, level, entityColliders, boundingBox.expandTowards(movement));
		}
		return EntityCollisions.collideWithShapes(movement, boundingBox, colliders);
	}
}
