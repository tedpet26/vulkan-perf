package dev.vulkanperf.client.mixin.imfast.accessors;

import net.minecraft.client.gui.render.DynamicAtlasAllocator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(DynamicAtlasAllocator.class)
public interface DynamicAtlasAllocatorAccessor {
	@Accessor("usedSlotByKey")
	Map<?, DynamicAtlasAllocator.Slot> vulkanperf$usedSlotByKey();
}
