package dev.vulkanperf.logic.entity;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Iterables;

import dev.vulkanperf.mixin.logic.collision.ServerLevelEntityManagerAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Continuation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Shared collision helpers: lazy collider materialisation for entity movement and
 * class-group-filtered hard-collision queries (clean-room rewrite of upstream
 * techniques, see NOTICE).
 */
public final class EntityCollisions {
	private EntityCollisions() {
	}

	/**
	 * Equivalent of vanilla {@code Entity#collectCollidersIgnoringWorldBorder}, but the block
	 * shapes are supplied through a {@link LazyColliderList} so no per-shape allocation happens
	 * until the per-axis sweep actually reaches them. Entity shapes and the world border shape
	 * keep their vanilla positions in the iteration order.
	 */
	public static List<VoxelShape> collectCollidersLazy(Entity source, net.minecraft.world.level.Level level, List<VoxelShape> entityColliders, AABB box) {
		boolean borderNear = source != null && level.getWorldBorder().isInsideCloseToBorder(source, box);
		if (entityColliders.isEmpty() && !borderNear) {
			return LazyColliderList.of(level.getBlockCollisions(source, box));
		}
		Iterable<VoxelShape> borderShapes = borderNear ? List.of(level.getWorldBorder().getCollisionShape()) : Collections.emptyList();
		return LazyColliderList.of(Iterables.concat(entityColliders, borderShapes, level.getBlockCollisions(source, box)));
	}

	/** Context flavour used by the {@code CollisionContext} overload of {@code collideBoundingBox}. */
	public static List<VoxelShape> collectCollidersLazy(net.minecraft.world.phys.shapes.CollisionContext source, net.minecraft.world.level.Level level, List<VoxelShape> entityColliders, AABB box) {
		if (entityColliders.isEmpty()) {
			return LazyColliderList.of(level.getBlockCollisionsFromContext(source, box));
		}
		return LazyColliderList.of(Iterables.concat(entityColliders, level.getBlockCollisionsFromContext(source, box)));
	}

	/** Vanilla-equivalent materialising collector (used when the optimization is disabled). */
	public static List<VoxelShape> collectVanilla(Entity source, net.minecraft.world.level.Level level, List<VoxelShape> entityColliders, AABB box) {
		com.google.common.collect.ImmutableList.Builder<VoxelShape> colliders = com.google.common.collect.ImmutableList
			.builderWithExpectedSize(entityColliders.size() + 1);
		if (!entityColliders.isEmpty()) {
			colliders.addAll(entityColliders);
		}
		if (source != null && level.getWorldBorder().isInsideCloseToBorder(source, box)) {
			colliders.add(level.getWorldBorder().getCollisionShape());
		}
		level.getBlockCollisions(source, box).forEach(colliders::add);
		return colliders.build();
	}

	/** Vanilla-equivalent materialising collector for the {@link CollisionContext} overload. */
	public static List<VoxelShape> collectVanilla(net.minecraft.world.phys.shapes.CollisionContext source, net.minecraft.world.level.Level level, List<VoxelShape> entityColliders, AABB box) {
		com.google.common.collect.ImmutableList.Builder<VoxelShape> colliders = com.google.common.collect.ImmutableList
			.builderWithExpectedSize(entityColliders.size() + 1);
		if (!entityColliders.isEmpty()) {
			colliders.addAll(entityColliders);
		}
		level.getBlockCollisionsFromContext(source, box).forEach(colliders::add);
		return colliders.build();
	}

	/**
	 * Replacement for the plain {@code getEntities(except, box, predicate)} call inside
	 * {@code EntityGetter#getEntityCollisions}: walks the entity sections and only evaluates the
	 * caller predicate for entities whose class can possibly collide at all (see
	 * {@link EntityCollisionGroups}). Identical results; the cost drops from "every entity in the
	 * box" to "every potentially-collidable entity in the box".
	 */
	public static List<Entity> getCollidingEntities(ServerLevel level, Entity except, AABB box, Predicate<Entity> predicate) {
		List<Entity> result = null;
		var storage = ((PersistentEntitySectionManagerAccess) (Object) ((ServerLevelEntityManagerAccessor) level).vp$getEntityManager()).vp$getSectionStorage();
		for (var iterator = new SectionIterator(storage, box); iterator.hasNext(); ) {
			Object storageMap = ((EntitySectionAccess) iterator.next()).vp$getStorage();
			List<Entity> group = ((CollisionGroupIndexed) storageMap).vp$getCollisionGroup();
			for (int i = 0; i < group.size(); i++) {
				Entity entity = group.get(i);
				if (entity != except
						&& entity.getBoundingBox().intersects(box)
						&& predicate.test(entity)) {
					if (result == null) {
						result = new java.util.ArrayList<>();
					}
					result.add(entity);
				}
			}
		}
		return result == null ? Collections.emptyList() : result;
	}

	/**
	 * Slightly unusual: the storage's iteration API is consumer-based and allocating a lambda per
	 * query showed up in profiles, so sections are pulled eagerly into a reused buffer instead.
	 */
	private static final class SectionIterator implements java.util.Iterator<net.minecraft.world.level.entity.EntitySection<Entity>> {
		private final java.util.ArrayList<net.minecraft.world.level.entity.EntitySection<Entity>> sections = new java.util.ArrayList<>(16);
		private int cursor;

		SectionIterator(net.minecraft.world.level.entity.EntitySectionStorage<Entity> storage, AABB box) {
			storage.forEachAccessibleNonEmptySection(box, section -> {
				this.sections.add(section);
				return Continuation.CONTINUE;
			});
		}

		@Override
		public boolean hasNext() {
			return this.cursor < this.sections.size();
		}

		@Override
		public net.minecraft.world.level.entity.EntitySection<Entity> next() {
			return this.sections.get(this.cursor++);
		}
	}

	/** Replicates the tail of vanilla {@code collideBoundingBox}: per-axis sweep against the collider list. */
	public static Vec3 collideWithShapes(Vec3 movement, AABB boundingBox, List<VoxelShape> shapes) {
		if (shapes.isEmpty()) {
			return movement;
		}
		Vec3 resolvedMovement = Vec3.ZERO;
		for (net.minecraft.core.Direction.Axis axis : net.minecraft.core.Direction.axisStepOrder(movement)) {
			double axisMovement = movement.get(axis);
			if (axisMovement != 0.0) {
				double collision = net.minecraft.world.phys.shapes.Shapes.collide(axis, boundingBox.move(resolvedMovement), shapes, axisMovement);
				resolvedMovement = resolvedMovement.with(axis, collision);
			}
		}
		return resolvedMovement;
	}

	/** Duck: exposes the section storage of {@code PersistentEntitySectionManager}. */
	public interface PersistentEntitySectionManagerAccess {
		net.minecraft.world.level.entity.EntitySectionStorage<Entity> vp$getSectionStorage();
	}

	/** Duck: exposes the backing multimap of an {@code EntitySection}. */
	public interface EntitySectionAccess {
		net.minecraft.util.ClassInstanceMultiMap<?> vp$getStorage();
	}

	/** Duck: per-multimap index of entities whose class may override the collision contract. */
	public interface CollisionGroupIndexed {
		List<Entity> vp$getCollisionGroup();
	}
}
