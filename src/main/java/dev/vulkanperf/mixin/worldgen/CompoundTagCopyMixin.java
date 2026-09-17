package dev.vulkanperf.mixin.worldgen;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.gen.Invoker;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * {@code CompoundTag#copy} builds a plain {@code HashMap} through resizes; a pre-sized fastutil
 * map removes the churn for chunk NBT copies (saving, network sync, worldgen blending).
 */
@Mixin(CompoundTag.class)
public abstract class CompoundTagCopyMixin {
	@Shadow
	@Final
	private Map<String, Tag> tags;

	@Invoker("<init>")
	static CompoundTag vp$create(Map<String, Tag> tags) {
		throw new AssertionError();
	}

	/**
	 * @reason pre-sized fastutil backing map
	 * @author vulkan-perf
	 */
	@Overwrite
	public CompoundTag copy() {
		Map<String, Tag> newTags = new Object2ObjectOpenHashMap<>(Math.max(4, this.tags.size() * 2));
		for (Map.Entry<String, Tag> entry : this.tags.entrySet()) {
			newTags.put(entry.getKey(), entry.getValue().copy());
		}
		return vp$create(newTags);
	}
}
