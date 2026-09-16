package dev.vulkanperf.mixin.logic;

import dev.vulkanperf.chunks.ChunkWorkers;
import dev.vulkanperf.config.PerfConfig;
import dev.vulkanperf.logic.EntitySectionIndex;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

@Mixin(Level.class)
public abstract class LevelCollisionMixin {
	@Unique
	private final EntitySectionIndex vulkanperf$entityQueryCache = new EntitySectionIndex();

	@Inject(method = "getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$reuseEntityQuery(Entity except, AABB box, Predicate<? super Entity> predicate, CallbackInfoReturnable<List<Entity>> cir) {
		if (!PerfConfig.get().logic.collisionCache || ChunkWorkers.isChunkWorker() || box == null) {
			return;
		}
		List<Entity> hit = vulkanperf$entityQueryCache.hit(((Level) (Object) this).getGameTime(), except, box, predicate);
		if (hit != null) {
			cir.setReturnValue(hit);
		}
	}

	@Inject(method = "getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;", at = @At("RETURN"))
	private void vulkanperf$storeEntityQuery(Entity except, AABB box, Predicate<? super Entity> predicate, CallbackInfoReturnable<List<Entity>> cir) {
		if (!PerfConfig.get().logic.collisionCache || ChunkWorkers.isChunkWorker() || box == null) {
			return;
		}
		List<Entity> list = cir.getReturnValue();
		if (list != null) {
			vulkanperf$entityQueryCache.store(((Level) (Object) this).getGameTime(), except, box, predicate, list);
		}
	}
}
