package dev.vulkanperf.client.mixin.imfast;

import com.mojang.renderpearl.api.pipeline.ShaderType;
import dev.vulkanperf.VulkanPerf;
import dev.vulkanperf.client.imfast.CoreTextShaders;
import dev.vulkanperf.client.imfast.ImFastRuntime;
import dev.vulkanperf.client.imfast.ResourcePackFeatureMetadata;
import dev.vulkanperf.client.mixin.imfast.accessors.MinecraftFontManagerAccessor;
import dev.vulkanperf.config.PerfConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Mixin(ShaderManager.class)
public abstract class ShaderManagerConflictMixin {
	@Inject(
		method = "apply(Lcom/mojang/renderpearl/api/device/GpuDevice;Lnet/minecraft/client/renderer/ShaderManager$PendingResults;)V",
		at = @At("RETURN")
	)
	private void vulkanperf$resourcePackAtlasGuard(CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();
		ResourceManager manager = client.getResourceManager();
		PackResources fontConflict = null;
		PackResources mapConflict = null;
		try {
			Set<PackResources> suspects = new HashSet<>();
			for (Identifier shader : CoreTextShaders.ids()) {
				addIfCustom(manager, ShaderType.VERTEX.idConverter().idToFile(shader), client, suspects);
				addIfCustom(manager, ShaderType.FRAGMENT.idConverter().idToFile(shader), client, suspects);
			}
			for (PackResources pack : suspects) {
				ResourcePackFeatureMetadata meta = pack.getMetadataSection(ResourcePackFeatureMetadata.TYPE);
				if (meta == null) {
					meta = ResourcePackFeatureMetadata.DEFAULT;
				}
				if (!meta.compatibleFeatures().contains("font_atlas_resizing")) {
					fontConflict = pack;
				}
				if (meta.incompatibleFeatures().contains("map_atlas_generation")) {
					mapConflict = pack;
				}
			}
		} catch (IOException e) {
			VulkanPerf.LOGGER.error("Failed to inspect resource packs for imfast atlas conflicts", e);
		}

		if (ImFastRuntime.enabled()) {
			if (fontConflict != null) {
				if (ImFastRuntime.fontAtlasResizing()) {
					VulkanPerf.LOGGER.warn("Resource pack {} replaces core text shaders; disabling font atlas resize", fontConflict.packId());
					ImFastRuntime.setFontAtlasResizing(false);
					this.vulkanperf$reloadFonts(client);
				}
			} else if (!ImFastRuntime.fontAtlasResizing() && PerfConfig.get().imfast.fontAtlasResizing) {
				VulkanPerf.LOGGER.info("Re-enabling font atlas resize; no conflicting text shaders");
				ImFastRuntime.setFontAtlasResizing(true);
				this.vulkanperf$reloadFonts(client);
			}
			if (mapConflict != null) {
				if (ImFastRuntime.mapAtlasGeneration()) {
					VulkanPerf.LOGGER.warn("Resource pack {} is marked incompatible with map atlases; disabling", mapConflict.packId());
					ImFastRuntime.setMapAtlasGeneration(false);
					client.getMapTextureManager().resetData();
				}
			} else if (!ImFastRuntime.mapAtlasGeneration() && PerfConfig.get().imfast.mapAtlasGeneration) {
				VulkanPerf.LOGGER.info("Re-enabling map atlases; no conflicting resource packs");
				ImFastRuntime.setMapAtlasGeneration(true);
				client.getMapTextureManager().resetData();
			}
		}
	}

	@Unique
	private static void addIfCustom(ResourceManager manager, Identifier file, Minecraft client, Set<PackResources> out) {
		manager.getResource(file).map(Resource::source).ifPresent(pack -> {
			if (!pack.equals(client.getVanillaPackResources())) {
				out.add(pack);
			}
		});
	}

	@Unique
	private void vulkanperf$reloadFonts(Minecraft client) {
		((MinecraftFontManagerAccessor) client).vulkanperf$fontManager().updateOptions(client.options);
	}
}
