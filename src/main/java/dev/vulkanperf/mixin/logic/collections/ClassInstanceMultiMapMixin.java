package dev.vulkanperf.mixin.logic.collections;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.vulkanperf.logic.entity.EntityCollisionGroups;
import dev.vulkanperf.logic.entity.EntityCollisions;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.util.ClassInstanceMultiMap;

/**
 * Two optimizations on the per-entity-section type map:
 *
 * <ul>
 * <li>{@code find(Class)} looks the per-class list up directly instead of streaming
 * {@code allInstances} through {@code isInstance} filters on every cache miss.</li>
 * <li>A reference-indexed side list of entities whose class may participate in hard collisions
 * (see {@link EntityCollisionGroups}) backs the collision-query fast path; most sections stay
 * empty here, which lets collision lookups skip whole sections.</li>
 * </ul>
 */
@Mixin(ClassInstanceMultiMap.class)
public abstract class ClassInstanceMultiMapMixin<T> implements EntityCollisions.CollisionGroupIndexed {
	@Shadow
	@Final
	@Mutable
	private Map<Class<?>, List<T>> byClass;

	@Shadow
	@Final
	private List<T> allInstances;

	@Shadow
	@Final
	private Class<T> baseClass;

	private final List<net.minecraft.world.entity.Entity> vp$collisionGroup = new ReferenceArrayList<>(0);

	@Inject(method = "<init>", at = @At("RETURN"))
	private void vp$swapByClassMap(Class<?> baseClass, CallbackInfo ci) {
		// Reference equality on class keys; keeps add() free of boxed hashing.
		this.byClass = new it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap<>(this.byClass);
	}

	@Inject(method = "add", at = @At("HEAD"))
	private void vp$onAdd(T instance, CallbackInfoReturnable<Boolean> cir) {
		if (instance instanceof net.minecraft.world.entity.Entity entity && EntityCollisionGroups.mayParticipateInCollision(entity)) {
			this.vp$collisionGroup.add(entity);
		}
	}

	@Inject(method = "remove", at = @At("HEAD"))
	private void vp$onRemove(Object object, CallbackInfoReturnable<Boolean> cir) {
		if (object instanceof net.minecraft.world.entity.Entity) {
			this.vp$collisionGroup.remove(object);
		}
	}

	@Override
	public List<net.minecraft.world.entity.Entity> vp$getCollisionGroup() {
		return this.vp$collisionGroup;
	}

	/**
	 * Direct map lookup replaces the cache-miss stream filter; the cached entry semantics are
	 * kept (an unmodifiable view of a list that vanilla fills once per class).
	 */
	@Inject(method = "find", at = @At("HEAD"), cancellable = true)
	private <S> void vp$findDirect(Class<S> index, CallbackInfoReturnable<java.util.Collection<S>> cir) {
		if (!this.baseClass.isAssignableFrom(index)) {
			// Fall through so vanilla throws its IllegalArgumentException.
			return;
		}
		List<T> found = this.byClass.get(index);
		if (found == null) {
			// Replicate the vanilla cache-fill pass so later lookups stay O(1).
			java.util.List<T> matches = new java.util.ArrayList<>();
			for (T instance : this.allInstances) {
				if (index.isInstance(instance)) {
					matches.add(instance);
				}
			}
			this.byClass.put(index, matches);
			found = matches;
		}
		@SuppressWarnings("unchecked")
		java.util.Collection<S> cast = (java.util.Collection<S>) Collections.unmodifiableCollection(found);
		cir.setReturnValue(cast);
	}
}
