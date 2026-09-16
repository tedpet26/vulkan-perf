package dev.vulkanperf.client.mixin.imfast;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.vulkanperf.client.imfast.map.MapAtlasAllocator;
import dev.vulkanperf.client.imfast.map.MapAtlasTexture;
import dev.vulkanperf.client.imfast.map.MapTextureManagerExtension;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.MapTextureManager;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.resources.MapTextureManager$MapInstance")
public abstract class MapInstanceMixin {
	@Shadow
	@Final
	private DynamicTexture texture;

	@Unique
	private AbstractTexture vulkanperf$atlasTexture;
	@Unique
	private int vulkanperf$atlasX;
	@Unique
	private int vulkanperf$atlasY;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void vulkanperf$bindAtlasSlot(MapTextureManager manager, int id, MapItemSavedData data, CallbackInfo ci) {
		MapAtlasAllocator allocator = ((MapTextureManagerExtension) manager).vulkanperf$mapAtlasAllocator();
		int location = allocator.getLocation(id);
		if (location == -1) {
			return;
		}
		MapAtlasTexture atlas = allocator.getAtlas(MapAtlasAllocator.atlasIdFromLocation(location));
		if (atlas == null) {
			return;
		}
		this.vulkanperf$atlasTexture = atlas;
		this.vulkanperf$atlasX = MapAtlasAllocator.slotXFromLocation(location) * MapAtlasAllocator.MAP_SIZE;
		this.vulkanperf$atlasY = MapAtlasAllocator.slotYFromLocation(location) * MapAtlasAllocator.MAP_SIZE;
	}

	@Inject(method = "updateTextureIfNeeded", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/DynamicTexture;upload()V", shift = At.Shift.AFTER))
	private void vulkanperf$uploadAtlasSlot(CallbackInfo ci) {
		if (this.vulkanperf$atlasTexture != null && this.texture.getPixels() != null) {
			RenderSystem.getDevice().createCommandEncoder().writeToTexture(
				this.vulkanperf$atlasTexture.getTexture(),
				this.texture.getPixels(),
				0,
				0,
				this.vulkanperf$atlasX,
				this.vulkanperf$atlasY
			);
		}
	}
}
