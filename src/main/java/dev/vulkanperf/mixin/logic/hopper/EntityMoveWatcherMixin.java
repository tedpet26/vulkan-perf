package dev.vulkanperf.mixin.logic.hopper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.vulkanperf.logic.hopper.ContainerChangeBus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Entity movement is a wake source for sleeping hoppers: anything (dropped items, container
 * minecarts, water-carried stacks) whose bounding box enters the watched pickup zone wakes the
 * sleeper. Gated by a registry-empty fast path so worlds without sleeping hoppers pay nothing.
 */
@Mixin(Entity.class)
public abstract class EntityMoveWatcherMixin {

	@Shadow
	public abstract Level level();

	@Inject(method = "move", at = @At("HEAD"))
	private void vulkanperf$wakeSleepersOnMove(MoverType moverType, Vec3 delta, CallbackInfo ci) {
		if (ContainerChangeBus.hasWatchers() && this.level() instanceof ServerLevel serverLevel) {
			ContainerChangeBus.onEntityBoxMoved(serverLevel, (Entity) (Object) this);
		}
	}
}
