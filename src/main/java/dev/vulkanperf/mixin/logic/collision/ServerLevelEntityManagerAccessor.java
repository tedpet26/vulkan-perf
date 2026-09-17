package dev.vulkanperf.mixin.logic.collision;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;

@Mixin(ServerLevel.class)
public interface ServerLevelEntityManagerAccessor {
	@Accessor("entityManager")
	PersistentEntitySectionManager<net.minecraft.world.entity.Entity> vp$getEntityManager();
}
