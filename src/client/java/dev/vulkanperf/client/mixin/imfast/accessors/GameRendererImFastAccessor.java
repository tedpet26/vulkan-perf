package dev.vulkanperf.client.mixin.imfast.accessors;

import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface GameRendererImFastAccessor {
	@Accessor("fogRenderer")
	FogRenderer vulkanperf$fogRenderer();

	@Accessor("guiRenderer")
	GuiRenderer vulkanperf$guiRenderer();
}
