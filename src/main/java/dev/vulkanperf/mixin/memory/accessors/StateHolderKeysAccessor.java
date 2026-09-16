package dev.vulkanperf.mixin.memory.accessors;

import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the property key array every {@link StateHolder} carries so the
 * FastMap-style table builder can read it without touching private fields
 * via reflection.
 */
@Mixin(StateHolder.class)
public interface StateHolderKeysAccessor {
	@Accessor("propertyKeys")
	Property<?>[] vulkanperf$getPropertyKeys();
}
