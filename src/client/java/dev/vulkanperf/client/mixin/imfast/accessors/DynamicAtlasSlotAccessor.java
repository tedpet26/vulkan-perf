package dev.vulkanperf.client.mixin.imfast.accessors;

import net.minecraft.client.gui.render.DynamicAtlasAllocator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DynamicAtlasAllocator.Slot.class)
public interface DynamicAtlasSlotAccessor {
	@Accessor("fresh")
	void vulkanperf$setFresh(boolean fresh);
}
