package dev.vulkanperf.mixin.logic.collision;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import dev.vulkanperf.logic.entity.EntityCollisions;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;

@Mixin(PersistentEntitySectionManager.class)
public abstract class PersistentEntitySectionManagerAccessor implements EntityCollisions.PersistentEntitySectionManagerAccess {
	@Shadow
	@Final
	private EntitySectionStorage<net.minecraft.world.entity.Entity> sectionStorage;

	@Override
	public EntitySectionStorage<net.minecraft.world.entity.Entity> vp$getSectionStorage() {
		return this.sectionStorage;
	}
}
