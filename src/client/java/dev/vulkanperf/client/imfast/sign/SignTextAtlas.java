package dev.vulkanperf.client.imfast.sign;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import dev.vulkanperf.client.imfast.ImFastRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.resources.Identifier;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

/**
 * Off-screen color+depth target used as a packed atlas of pre-rasterized sign text.
 */
public final class SignTextAtlas extends TextureTarget implements AutoCloseable {
	private static final Vector4f CLEAR_COLOR = new Vector4f(0.0F);
	private final Identifier textureId;
	private final Slot root;

	public SignTextAtlas() {
		super("vulkanperf sign atlas", ImFastRuntime.signAtlasSize(), ImFastRuntime.signAtlasSize(), GpuFormat.RGBA8_UNORM, GpuFormat.D32_FLOAT);
		this.textureId = Identifier.fromNamespaceAndPath("vulkanperf", "sign_atlas/0");
		Minecraft.getInstance().getTextureManager().register(this.textureId, new BoundColorTexture(this));
		int size = ImFastRuntime.signAtlasSize();
		this.root = new Slot(null, 0, 0, size, size);
	}

	public static int atlasSize() {
		return ImFastRuntime.signAtlasSize();
	}

	public Identifier textureId() {
		return this.textureId;
	}

	public @Nullable Slot findSlot(int width, int height) {
		return this.root.findSlot(width, height);
	}

	public void clearAtlas() {
		RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(this.getColorTexture(), CLEAR_COLOR, this.getDepthTexture(), 0.0);
		this.root.childA = null;
		this.root.childB = null;
		this.root.occupied = false;
	}

	@Override
	public void close() {
		Minecraft.getInstance().getTextureManager().release(this.textureId);
		this.destroyBuffers();
	}

	public final class Slot {
		public final int x;
		public final int y;
		public final int width;
		public final int height;
		private final @Nullable Slot parent;
		private @Nullable Slot childA;
		private @Nullable Slot childB;
		private boolean occupied;

		private Slot(@Nullable Slot parent, int x, int y, int width, int height) {
			this.parent = parent;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		public void markFree() {
			if (this.childA != null || this.childB != null) {
				throw new IllegalStateException("Cannot free a split atlas slot");
			}
			if (!this.occupied) {
				throw new IllegalStateException("Slot is already free");
			}
			this.occupied = false;
			collapseEmpty(this);
			int atlas = atlasSize();
			RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
				SignTextAtlas.this.getColorTexture(),
				CLEAR_COLOR,
				SignTextAtlas.this.getDepthTexture(),
				0.0,
				this.x,
				atlas - this.y - this.height,
				this.width,
				this.height,
				0
			);
		}

		private @Nullable Slot findSlot(int width, int height) {
			if (this.childA != null && this.childB != null) {
				Slot found = this.childA.findSlot(width, height);
				return found != null ? found : this.childB.findSlot(width, height);
			}
			if (this.occupied || width > this.width || height > this.height) {
				return null;
			}
			if (width == this.width && height == this.height) {
				this.occupied = true;
				return this;
			}
			int extraX = this.width - width;
			int extraY = this.height - height;
			if (extraX > extraY) {
				this.childA = new Slot(this, this.x, this.y, width, this.height);
				this.childB = new Slot(this, this.x + width, this.y, this.width - width, this.height);
			} else {
				this.childA = new Slot(this, this.x, this.y, this.width, height);
				this.childB = new Slot(this, this.x, this.y + height, this.width, this.height - height);
			}
			return this.childA.findSlot(width, height);
		}

		private static void collapseEmpty(Slot slot) {
			if (slot == null) {
				return;
			}
			collapseEmpty(slot.parent);
			boolean aEmpty = slot.childA != null && !hasOccupied(slot.childA);
			boolean bEmpty = slot.childB != null && !hasOccupied(slot.childB);
			if (aEmpty && bEmpty) {
				slot.childA = null;
				slot.childB = null;
			}
		}

		private static boolean hasOccupied(Slot slot) {
			if (slot == null) {
				return false;
			}
			if (slot.occupied) {
				return true;
			}
			return hasOccupied(slot.childA) || hasOccupied(slot.childB);
		}
	}

	private static final class BoundColorTexture extends AbstractTexture implements Dumpable {
		private BoundColorTexture(SignTextAtlas atlas) {
			this.texture = atlas.getColorTexture();
			this.textureView = atlas.getColorTextureView();
			this.sampler = RenderSystem.getSamplerCache().getSampler(
				AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.NEAREST, FilterMode.NEAREST, false
			);
		}

		@Override
		public void close() {
		}

		@Override
		public void dumpContents(Identifier selfId, Path dir) {
			TextureUtil.writeAsPNG(dir, selfId.toDebugFileName(), this.getTexture(), 0, argb -> argb);
		}
	}
}
