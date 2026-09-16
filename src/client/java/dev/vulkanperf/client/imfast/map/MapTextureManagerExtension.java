package dev.vulkanperf.client.imfast.map;

/**
 * Implemented on {@code net.minecraft.client.resources.MapTextureManager} via mixin so the map
 * atlas allocator lives alongside the manager it packs textures for (one allocator per world/manager
 * instance lifetime, reset together with the manager's own map cache).
 */
public interface MapTextureManagerExtension {
	MapAtlasAllocator vulkanperf$mapAtlasAllocator();
}
