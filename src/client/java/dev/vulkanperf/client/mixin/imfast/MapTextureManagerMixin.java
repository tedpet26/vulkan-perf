package dev.vulkanperf.client.mixin.imfast;

import dev.vulkanperf.client.imfast.ImFastRuntime;
import dev.vulkanperf.client.imfast.map.MapAtlasAllocator;
import dev.vulkanperf.client.imfast.map.MapTextureManagerExtension;
import net.minecraft.client.resources.MapTextureManager;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MapTextureManager.class)
public abstract class MapTextureManagerMixin implements MapTextureManagerExtension {
	@Unique
	private final MapAtlasAllocator vulkanperf$allocator = new MapAtlasAllocator();

	@Inject(method = "resetData", at = @At("RETURN"))
	private void vulkanperf$resetMapAtlases(CallbackInfo ci) {
		this.vulkanperf$allocator.reset();
	}

	@Inject(method = "getOrCreateMapInstance", at = @At("HEAD"))
	private void vulkanperf$reserveAtlasSlot(MapId id, MapItemSavedData data, CallbackInfoReturnable<?> cir) {
		if (ImFastRuntime.mapAtlasGeneration()) {
			this.vulkanperf$allocator.getOrAllocateLocation(id.id());
		}
	}

	@Override
	public MapAtlasAllocator vulkanperf$mapAtlasAllocator() {
		return this.vulkanperf$allocator;
	}
}
