package dev.vulkanperf.memory.components;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import net.minecraft.core.component.DataComponentType;

/**
 * Every item stack that has never had a component patch applied (the vast
 * majority, in most worlds) ends up with its own tiny-but-nonzero empty patch
 * map. Swapping those out for one shared immutable empty map removes that
 * per-stack allocation once the patch reverts to empty.
 */
public final class EmptyPatchSharing {
	private EmptyPatchSharing() {
	}

	public static Reference2ObjectMap<DataComponentType<?>, Object> sharedEmptyPatch() {
		return Reference2ObjectMaps.emptyMap();
	}
}
