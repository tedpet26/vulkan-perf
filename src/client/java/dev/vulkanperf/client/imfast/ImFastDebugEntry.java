package dev.vulkanperf.client.imfast;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ImFastDebugEntry implements DebugScreenEntry {
	public static final Identifier ID = Identifier.fromNamespaceAndPath("vulkanperf", "imfast");

	@Override
	public void display(DebugScreenDisplayer displayer, @Nullable Level level, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
		List<String> lines = new ArrayList<>();
		lines.add("vulkan-perf imfast");
		Minecraft client = Minecraft.getInstance();
		if (client.getMapTextureManager() instanceof dev.vulkanperf.client.imfast.map.MapTextureManagerExtension maps) {
			var allocator = maps.vulkanperf$mapAtlasAllocator();
			int mapsPacked = 0;
			for (var atlas : allocator.atlases()) {
				mapsPacked += atlas.mapCount();
			}
			lines.add("Map atlas: " + allocator.atlases().size() + "x" + allocator.atlasSizePixels() + " (" + mapsPacked + " maps)");
		}
		if (client.gameRenderer != null) {
			var gui = ((dev.vulkanperf.client.mixin.imfast.accessors.GameRendererImFastAccessor) client.gameRenderer).vulkanperf$guiRenderer();
			if (gui instanceof dev.vulkanperf.client.imfast.gui.GuiRendererExtension ext) {
				var animated = ext.vulkanperf$animatedItemAtlas();
				if (animated != null) {
					lines.add("Animated item atlas: " + animated.textureSize() + " (" + animated.lastUsedSlotCount() + " items)");
				} else {
					lines.add("Animated item atlas: unused");
				}
			}
		}
		if (ImFastRuntime.signTextCache() != null) {
			lines.add("Sign text cache: " + ImFastRuntime.signTextCache().slots.size() + " entries");
		}
		displayer.addToGroup(ID, lines);
	}

	@Override
	public boolean isAllowed(boolean reducedDebugInfo) {
		return true;
	}
}
