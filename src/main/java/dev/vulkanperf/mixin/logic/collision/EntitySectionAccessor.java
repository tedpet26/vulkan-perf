package dev.vulkanperf.mixin.logic.collision;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import dev.vulkanperf.logic.entity.EntityCollisions;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.level.entity.EntitySection;

@Mixin(EntitySection.class)
public abstract class EntitySectionAccessor implements EntityCollisions.EntitySectionAccess {
	@Shadow
	@Final
	private ClassInstanceMultiMap<net.minecraft.world.level.entity.EntityAccess> storage;

	@Override
	public ClassInstanceMultiMap<?> vp$getStorage() {
		return this.storage;
	}
}
