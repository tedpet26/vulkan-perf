package dev.vulkanperf.mixin.logic.collections;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.util.ClassInstanceMultiMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Caches per-class filter views of the entity collection so repeated
 * {@code getByClass} calls within a tick are O(1).
 */
@Mixin(ClassInstanceMultiMap.class)
public abstract class ClassInstanceMultiMapMixin {
	@Unique
	private final Map<Class<?>, List<?>> vulkanperf$filterCache = new IdentityHashMap<>();

	@Unique
	private boolean vulkanperf$dirty = true;

	@Unique
	private <T> List<?> vulkanperf$getCached(Class<T> type) {
		return this.vulkanperf$dirty ? null : this.vulkanperf$filterCache.get(type);
	}

	@Unique
	private void vulkanperf$storeCached(Class<?> type, List<?> list) {
		this.vulkanperf$filterCache.put(type, list);
		this.vulkanperf$dirty = false;
	}

	@Unique
	private void vulkanperf$invalidate() {
		if (PerfConfig.get().logic.entityTypeFiltering) {
			if (!this.vulkanperf$filterCache.isEmpty()) {
				this.vulkanperf$filterCache.clear();
			}
			this.vulkanperf$dirty = true;
		}
	}
}
