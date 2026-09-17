package dev.vulkanperf.logic.entity;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.entity.Entity;

/**
 * Classifies entity classes by their collision behaviour without evaluating it per query.
 *
 * <p>Vanilla {@link Entity#canBeCollidedWith(Entity)} returns {@code false} at the base class, so an
 * entity whose class (or any superclass below Entity) never overrides {@code canBeCollidedWith} or
 * {@code canCollideWith} can never take part in a hard collision query. The expensive full-box entity
 * scan can therefore skip those entities entirely and only evaluate the real predicate for the
 * (rare) classes that override the contract, e.g. boats, shulker boxes and minecarts.
 *
 * <p>The override check is computed once per class and cached.
 */
public final class EntityCollisionGroups {
	private static final ConcurrentHashMap<Class<?>, Boolean> CUSTOM_COLLISION_CACHE = new ConcurrentHashMap<>();

	private EntityCollisionGroups() {
	}

	/**
	 * Returns whether the entity's class hierarchy overrides {@code canBeCollidedWith} or
	 * {@code canCollideWith} somewhere below {@link Entity}. If false, the vanilla predicates
	 * are guaranteed to reject the entity, so it can be skipped without evaluation.
	 */
	public static boolean mayParticipateInCollision(Entity entity) {
		Class<?> clazz = entity.getClass();
		Boolean cached = CUSTOM_COLLISION_CACHE.get(clazz);
		if (cached == null) {
			cached = computeCustomCollision(clazz);
			CUSTOM_COLLISION_CACHE.put(clazz, cached);
		}
		return cached;
	}

	private static boolean computeCustomCollision(Class<?> clazz) {
		return overrides(clazz, "canBeCollidedWith") || overrides(clazz, "canCollideWith");
	}

	private static boolean overrides(Class<?> clazz, String methodName) {
		for (Class<?> current = clazz; current != null && current != Entity.class && current != Object.class; current = current.getSuperclass()) {
			for (Method method : current.getDeclaredMethods()) {
				if (method.getName().equals(methodName) && method.getParameterCount() == 1
						&& (method.getParameterTypes()[0] == Entity.class || method.getParameterTypes()[0] == Object.class)) {
					return true;
				}
			}
		}
		return false;
	}
}
