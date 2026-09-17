package dev.vulkanperf.mixin.logic.collision;

import java.util.List;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.vulkanperf.config.PerfConfig;
import dev.vulkanperf.logic.entity.EntityCollisions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.phys.AABB;

/**
 * Routes the entity scan inside {@code EntityGetter#getEntityCollisions} through the class-group
 * filtered query on server levels; every other getter keeps vanilla behaviour.
 */
@Mixin(EntityGetter.class)
public interface EntityGetterCollisionsMixin {

	@Redirect(
		method = "getEntityCollisions",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/EntityGetter;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"
		)
	)
	@SuppressWarnings({"unchecked", "rawtypes"})
	default List<Entity> vp$getCollidingEntities(EntityGetter getter, Entity except, AABB box, Predicate predicate) {
		PerfConfig.LogicConfig logic = PerfConfig.get().logic;
		if (logic.entityCollisionGroups && getter instanceof ServerLevel level) {
			return EntityCollisions.getCollidingEntities(level, except, box, predicate);
		}
		return getter.getEntities(except, box, predicate);
	}
}
