package dev.vulkanperf.client.mixin.imfast.accessors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.FontManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftFontManagerAccessor {
	@Accessor("fontManager")
	FontManager vulkanperf$fontManager();
}
