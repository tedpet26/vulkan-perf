package dev.vulkanperf.client.mixin.imfast;

import com.llamalad7.mixinextras.sugar.Local;
import dev.vulkanperf.client.imfast.ImFastRuntime;
import dev.vulkanperf.client.imfast.map.MapAtlasAllocator;
import dev.vulkanperf.client.imfast.map.MapAtlasTexture;
import dev.vulkanperf.client.imfast.map.MapRenderStateExtension;
import dev.vulkanperf.client.imfast.map.MapTextureManagerExtension;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.resources.MapTextureManager;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapRenderer.class)
public abstract class MapRendererMixin {
	@Shadow
	@Final
	private MapTextureManager mapTextureManager;

	@ModifyArg(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitCustomGeometry(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/SubmitNodeCollector$CustomGeometryRenderer;)V",
			ordinal = 0
		)
	)
	private SubmitNodeCollector.CustomGeometryRenderer vulkanperf$atlasMapGeometry(
		SubmitNodeCollector.CustomGeometryRenderer original,
		@Local(argsOnly = true) MapRenderState mapRenderState,
		@Local(argsOnly = true) int lightCoords
	) {
		MapRenderStateExtension ext = (MapRenderStateExtension) mapRenderState;
		if (ext.vulkanperf$getAtlasTextureId() == null || !ext.vulkanperf$getAtlasTextureId().equals(mapRenderState.texture)) {
			return original;
		}
		int atlasSize = ImFastRuntime.mapAtlasSize();
		float u0 = (float) ext.vulkanperf$getAtlasX() / atlasSize;
		float u1 = (float) (ext.vulkanperf$getAtlasX() + MapAtlasAllocator.MAP_SIZE) / atlasSize;
		float v0 = (float) ext.vulkanperf$getAtlasY() / atlasSize;
		float v1 = (float) (ext.vulkanperf$getAtlasY() + MapAtlasAllocator.MAP_SIZE) / atlasSize;
		int map = MapAtlasAllocator.MAP_SIZE;
		return (pose, buffer) -> {
			buffer.addVertex(pose, 0.0F, map, -0.01F).setColor(-1).setUv(u0, v1).setLight(lightCoords);
			buffer.addVertex(pose, map, map, -0.01F).setColor(-1).setUv(u1, v1).setLight(lightCoords);
			buffer.addVertex(pose, map, 0.0F, -0.01F).setColor(-1).setUv(u1, v0).setLight(lightCoords);
			buffer.addVertex(pose, 0.0F, 0.0F, -0.01F).setColor(-1).setUv(u0, v0).setLight(lightCoords);
		};
	}

	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void vulkanperf$attachAtlas(MapId mapId, MapItemSavedData mapData, MapRenderState mapRenderState, CallbackInfo ci) {
		MapRenderStateExtension ext = (MapRenderStateExtension) mapRenderState;
		if (!ImFastRuntime.mapAtlasGeneration()) {
			ext.vulkanperf$setAtlasTextureId(null);
			return;
		}
		MapAtlasAllocator allocator = ((MapTextureManagerExtension) this.mapTextureManager).vulkanperf$mapAtlasAllocator();
		int location = allocator.getLocation(mapId.id());
		if (location == -1) {
			ext.vulkanperf$setAtlasTextureId(null);
			ext.vulkanperf$setAtlasX(0);
			ext.vulkanperf$setAtlasY(0);
			return;
		}
		MapAtlasTexture atlas = allocator.getAtlas(MapAtlasAllocator.atlasIdFromLocation(location));
		if (atlas == null) {
			ext.vulkanperf$setAtlasTextureId(null);
			return;
		}
		ext.vulkanperf$setAtlasTextureId(atlas.textureId());
		ext.vulkanperf$setAtlasX(MapAtlasAllocator.slotXFromLocation(location) * MapAtlasAllocator.MAP_SIZE);
		ext.vulkanperf$setAtlasY(MapAtlasAllocator.slotYFromLocation(location) * MapAtlasAllocator.MAP_SIZE);
		mapRenderState.texture = atlas.textureId();
	}
}
