package dev.vulkanperf.client.mixin.imfast.accessors;

import com.mojang.renderpearl.api.textures.GpuTexture;
import net.minecraft.client.gui.render.DynamicAtlasAllocator;
import net.minecraft.client.gui.render.GuiItemAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiItemAtlas.class)
public interface GuiItemAtlasAccessor {
	@Accessor("texture")
	GpuTexture vulkanperf$texture();

	@Accessor("depthTexture")
	GpuTexture vulkanperf$depthTexture();

	@Accessor("allocator")
	DynamicAtlasAllocator<Object> vulkanperf$allocator();
}
