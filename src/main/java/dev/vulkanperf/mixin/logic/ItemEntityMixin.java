package dev.vulkanperf.mixin.logic;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Predicate;

/**
 * Caches empty item-merge neighbor scans so lonely item entities do not
 * re-query the section storage every merge attempt.
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
	@Unique
	private long vulkanperf$emptyUntil;

	@Redirect(
		method = "mergeWithNeighbours",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/EntityGetter;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"
		)
	)
	private List<ItemEntity> vulkanperf$cachedNeighbors(Level level, Class<ItemEntity> type, AABB box, Predicate<? super ItemEntity> predicate) {
		if (!PerfConfig.get().logic.itemMerge) {
			return level.getEntitiesOfClass(type, box, predicate);
		}
		long tick = level.getGameTime();
		if (tick < this.vulkanperf$emptyUntil) {
			return List.of();
		}
		List<ItemEntity> found = level.getEntitiesOfClass(type, box, predicate);
		if (found.isEmpty()) {
			this.vulkanperf$emptyUntil = tick + 10L;
		} else {
			this.vulkanperf$emptyUntil = Long.MIN_VALUE;
		}
		return found;
	}
}
