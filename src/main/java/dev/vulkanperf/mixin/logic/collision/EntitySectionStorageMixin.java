package dev.vulkanperf.mixin.logic.collision;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.SectionPos;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.phys.AABB;

/**
 * Small-box entity queries skip the ordered AVL subset walk: for boxes spanning at most 4x4
 * sections horizontally, direct {@code getSection} hash lookups in vanilla's exact traversal
 * order (x, then z, then y) are considerably cheaper than materialising iterators over the
 * {@code sectionIds} sub-range.
 */
@Mixin(EntitySectionStorage.class)
public abstract class EntitySectionStorageMixin<T extends net.minecraft.world.level.entity.EntityAccess> {

	@Shadow
	public abstract EntitySection<T> getSection(long key);

	@Inject(method = "forEachAccessibleNonEmptySection", at = @At("HEAD"), cancellable = true)
	private void vp$fastSmallBoxIteration(AABB bb, AbortableIterationConsumer<EntitySection<T>> output, CallbackInfo ci) {
		int xMin = SectionPos.posToSectionCoord(bb.minX - 2.0);
		int yMin = SectionPos.posToSectionCoord(bb.minY - 4.0);
		int zMin = SectionPos.posToSectionCoord(bb.minZ - 2.0);
		int xMax = SectionPos.posToSectionCoord(bb.maxX + 2.0);
		int yMax = SectionPos.posToSectionCoord(bb.maxY + 0.0);
		int zMax = SectionPos.posToSectionCoord(bb.maxZ + 2.0);

		// Huge boxes profit from the ordered walk's early sub-range skips; only take over when small.
		if (xMax - xMin > 3 || zMax - zMin > 3) {
			return;
		}

		for (int x = xMin; x <= xMax; x++) {
			for (int z = zMin; z <= zMax; z++) {
				for (int y = yMin; y <= yMax; y++) {
					EntitySection<T> section = this.getSection(SectionPos.asLong(x, y, z));
					if (section != null && !section.isEmpty() && section.getStatus().isAccessible()
							&& output.accept(section).shouldAbort()) {
						ci.cancel();
						return;
					}
				}
			}
		}
		ci.cancel();
	}
}
