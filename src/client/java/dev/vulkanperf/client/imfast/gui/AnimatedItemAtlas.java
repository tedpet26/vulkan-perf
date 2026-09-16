package dev.vulkanperf.client.imfast.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.util.Mth;

/**
 * Dedicated GUI item atlas that is fully cleared once per frame instead of clearing each
 * animated slot individually. Animated icons are discarded after the frame, so the next
 * allocation can treat slots as empty and skip per-slot clears.
 */
public final class AnimatedItemAtlas extends GuiItemAtlas {
	private static final int MINIMUM_TEXTURE_SIZE = 128;
	private int lastUsedSlotCount;

	public static int textureSizeFor(int slotTextureSize, int requiredSlotCount) {
		int preferredSlotCount = requiredSlotCount + requiredSlotCount / 2;
		int atlasSlots = Mth.smallestSquareSide(preferredSlotCount);
		return Math.clamp(
			Mth.smallestEncompassingPowerOfTwo(atlasSlots * slotTextureSize),
			MINIMUM_TEXTURE_SIZE,
			RenderSystem.getDevice().getDeviceInfo().limits().maxTextureSizeForFormat(GpuFormat.RGBA8_UNORM)
		);
	}

	public AnimatedItemAtlas(FeatureRenderDispatcher dispatcher, int textureSize, int slotTextureSize) {
		super(dispatcher, textureSize, slotTextureSize);
	}

	@Override
	public GuiItemAtlas.SlotView getOrUpdate(TrackingItemStackRenderState item) {
		if (!item.isAnimated()) {
			throw new IllegalArgumentException("Animated item atlas only accepts animated GUI items");
		}
		return super.getOrUpdate(item);
	}

	public void recordUsedSlots(int count) {
		this.lastUsedSlotCount = count;
	}

	public int lastUsedSlotCount() {
		return this.lastUsedSlotCount;
	}
}
