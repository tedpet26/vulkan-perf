package dev.vulkanperf.client.imfast.map;

import dev.vulkanperf.client.imfast.ImFastRuntime;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.Collection;

/**
 * Owned by a single {@code MapTextureManager}. Packs many 128x128 in-game map textures into a
 * handful of shared GPU atlas sheets so that rendering many held/placed maps does not require one
 * texture bind per map id.
 *
 * <p>Each allocated map gets an opaque {@code location} int, encoding (atlasId, slotX, slotY),
 * which the associated mixins decode to compute UVs into the shared atlas texture.
 */
public final class MapAtlasAllocator {
	public static final int MAP_SIZE = 128;

	private final Int2ObjectMap<MapAtlasTexture> atlases = new Int2ObjectOpenHashMap<>();
	private final Int2IntMap mapIdToLocation = new Int2IntOpenHashMap();

	public MapAtlasAllocator() {
		this.mapIdToLocation.defaultReturnValue(-1);
	}

	public int atlasSizePixels() {
		return ImFastRuntime.mapAtlasSize();
	}

	private int slotsPerRow() {
		return Math.max(1, this.atlasSizePixels() / MAP_SIZE);
	}

	private int maxSlotsPerAtlas() {
		int perRow = this.slotsPerRow();
		return perRow * perRow;
	}

	public static int atlasIdFromLocation(final int location) {
		return (location >>> 16) & 0xFFFF;
	}

	public static int slotXFromLocation(final int location) {
		return (location >>> 8) & 0xFF;
	}

	public static int slotYFromLocation(final int location) {
		return location & 0xFF;
	}

	public int getOrAllocateLocation(final int mapId) {
		int cached = this.mapIdToLocation.get(mapId);
		if (cached != -1) {
			return cached;
		}

		int location = this.allocateLocation();
		this.mapIdToLocation.put(mapId, location);
		return location;
	}

	public int getLocation(final int mapId) {
		return this.mapIdToLocation.get(mapId);
	}

	private int allocateLocation() {
		for (MapAtlasTexture atlas : this.atlases.values()) {
			int slot = atlas.mapCount();
			if (slot < maxSlotsPerAtlas()) {
				return this.encodeAndClaim(atlas);
			}
		}

		MapAtlasTexture atlas = new MapAtlasTexture(this.atlases.size(), this.atlasSizePixels());
		this.atlases.put(atlas.id(), atlas);
		return this.encodeAndClaim(atlas);
	}

	private int encodeAndClaim(final MapAtlasTexture atlas) {
		int slotsPerRow = this.slotsPerRow();
		int slot = atlas.mapCount();
		int slotX = slot % slotsPerRow;
		int slotY = slot / slotsPerRow;
		atlas.incrementMapCount();
		return (atlas.id() << 16) | (slotX << 8) | slotY;
	}

	public MapAtlasTexture getAtlas(final int atlasId) {
		return this.atlases.get(atlasId);
	}

	public Collection<MapAtlasTexture> atlases() {
		return this.atlases.values();
	}

	public void reset() {
		for (MapAtlasTexture atlas : this.atlases.values()) {
			atlas.release();
		}
		this.atlases.clear();
		this.mapIdToLocation.clear();
	}
}
