package dev.vulkanperf.mixin.logic.hopper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.vulkanperf.logic.hopper.ContainerChangeBus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

/**
 * Newly spawned entities may drop straight into a sleeping hopper's pickup zone without ever
 * calling {@code Entity#move}; the spawn path is therefore also a wake source.
 */
@Mixin(net.minecraft.world.level.entity.PersistentEntitySectionManager.class)
public class EntitySpawnWatcherMixin {

	@Inject(method = "addNewEntity", at = @At("TAIL"))
	private void vulkanperf$wakeSleepersOnSpawn(net.minecraft.world.level.entity.EntityAccess entityAccess, CallbackInfoReturnable<Boolean> cir) {
		if (ContainerChangeBus.hasWatchers() && entityAccess instanceof Entity entity
				&& entity.level() instanceof ServerLevel serverLevel
				&& ((dev.vulkanperf.mixin.logic.collision.ServerLevelEntityManagerAccessor) serverLevel).vp$getEntityManager() == (Object) this) {
			ContainerChangeBus.onEntityBoxMoved(serverLevel, entity);
		}
	}
}
