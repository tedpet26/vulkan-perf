package dev.vulkanperf.client.imfast.map;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;

/**
 * A single GPU-side texture sheet used to pack several 128x128 map textures together so that
 * rendering many maps only touches a handful of distinct textures instead of one per map id.
 *
 * <p>Registered with the vanilla {@link net.minecraft.client.renderer.texture.TextureManager} under
 * its own {@link #textureId()} so it can be referenced anywhere a plain map texture {@link Identifier}
 * would otherwise be used (e.g. {@code MapRenderState#texture}).
 */
public final class MapAtlasTexture extends AbstractTexture {
	private final int id;
	private final Identifier textureId;
	private int mapCount;

	public MapAtlasTexture(final int id, final int sizePixels) {
		this.id = id;
		GpuDevice device = RenderSystem.getDevice();
		this.texture = device.createTexture(
			() -> "vulkanperf/map_atlas_" + id, GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING, GpuFormat.RGBA8_UNORM, sizePixels, sizePixels, 1, 1
		);
		this.textureView = device.createTextureView(this.texture);
		this.sampler = RenderSystem.getSamplerCache().getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.NEAREST, FilterMode.NEAREST, false);
		this.textureId = Identifier.fromNamespaceAndPath("vulkanperf", "map_atlas/" + id);
		Minecraft.getInstance().getTextureManager().register(this.textureId, this);
	}

	public int id() {
		return this.id;
	}

	public Identifier textureId() {
		return this.textureId;
	}

	public int mapCount() {
		return this.mapCount;
	}

	public void incrementMapCount() {
		this.mapCount++;
	}

	public void write(final NativeImage pixels, final int destX, final int destY) {
		RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.getTexture(), pixels, 0, 0, destX, destY);
	}

	/**
	 * Unregisters this texture from the {@link net.minecraft.client.renderer.texture.TextureManager},
	 * which in turn calls {@link #close()} on it exactly once.
	 */
	public void release() {
		Minecraft.getInstance().getTextureManager().release(this.textureId);
	}
}
