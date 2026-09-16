package dev.vulkanperf.mixin;

import dev.vulkanperf.config.PerfConfig;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class VulkanPerfMixinPlugin implements IMixinConfigPlugin {
	@Override
	public void onLoad(String mixinPackage) {
		PerfConfig.load();
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		PerfConfig config = PerfConfig.get();
		String name = mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1);
		if (mixinClassName.contains(".logic.")) {
			if (!config.logic.enabled) {
				return false;
			}
			return switch (name) {
				case "LevelCollisionMixin" -> config.logic.collisionCache;
				case "HopperBlockEntityMixin", "HopperIdleMixin" -> config.logic.hopper;
				case "HopperSleepMixin" -> config.logic.hopper && config.logic.hopperSleep;
				case "BrainMixin" -> config.logic.inactiveAi;
				case "ShapesJoinMixin" -> config.logic.voxelShapes;
				case "ShapesJoinIsNotEmptyMixin" -> config.logic.joinIsNotEmptyCache;
				case "PathNavigationMixin" -> config.logic.pathCache;
				case "WalkNodeEvaluatorPathTypeMixin" -> config.logic.pathTypeCache;
				case "ItemEntityMixin" -> config.logic.itemMerge;
				case "MobAiMixin" -> config.logic.mobAiSkip;
				case "ClassInstanceMultiMapMixin" -> config.logic.entityTypeFiltering;
				case "LevelChunkSectionRandomTickMixin" -> config.logic.randomTickSkip;
				case "AbstractFurnaceSleepMixin", "BrewingStandSleepMixin" -> config.logic.sleepingBlockEntities;
				case "PoiManagerFindClosestMixin" -> config.logic.poiCache;
				default -> true;
			};
		}
		if (mixinClassName.contains(".chunks.")) {
			return config.chunks.enabled;
		}
		if (mixinClassName.contains(".chunksys.")) {
			if (!config.chunks.enabled) {
				return false;
			}
			return switch (name) {
				case "MinecraftServerSchedulingMixin" -> config.chunks.midTickScheduling || config.chunks.enhancedAutosave;
				case "ChunkMapLightingMixin" -> config.chunks.threadedLighting || config.chunks.viewDistanceDiagnostics;
				case "ChunkMapAsyncMixin" -> config.chunks.asyncSerializationHooks;
				case "RegionFileStorageCacheMixin" -> config.chunks.asyncIoDeepened;
				case "OptionsViewDistanceMixin" -> config.chunks.clientViewDistanceUncap;
				// Accessor mixins carry no behavior of their own; keep them applied
				// whenever any dependent feature above might need them.
				case "ChunkMapDistanceAccessor", "DistanceManagerAccessor" -> true;
				default -> true;
			};
		}
		if (mixinClassName.contains(".packets.")) {
			return config.packets.enabled;
		}
		if (mixinClassName.contains(".memory.")) {
			if (!config.memory.enabled) {
				return false;
			}
			if (mixinClassName.contains(".memory.fastmap.")) {
				return config.memory.fastMapNeighborLookup;
			}
			if (mixinClassName.contains(".memory.blockstate.")) {
				return config.memory.blockStateCacheDedup;
			}
			if (mixinClassName.contains(".memory.components.")) {
				return config.memory.dataComponentPatchSharing;
			}
			if (mixinClassName.contains(".memory.thread.")) {
				return config.memory.smallThreadDetector;
			}
			if (mixinClassName.contains(".memory.accessors.")) {
				return switch (name) {
					case "StateHolderKeysAccessor" -> config.memory.fastMapNeighborLookup;
					default -> config.memory.blockStateCacheDedup;
				};
			}
			return switch (name) {
				case "ShapesJoinCacheMixin" -> config.memory.internShapes;
				default -> true;
			};
		}
		if (mixinClassName.contains(".logging.")) {
			return config.logging.enabled;
		}
		if (mixinClassName.contains(".power.")) {
			return config.power.enabled;
		}
		if (mixinClassName.contains(".particles.")) {
			return config.particles.enabled;
		}
		if (mixinClassName.contains(".culling.")) {
			return config.culling.enabled;
		}
		if (mixinClassName.contains(".clientcache.")) {
			return config.clientcache.enabled;
		}
		if (mixinClassName.contains(".hudspread.")) {
			return config.hudspread.enabled;
		}
		if (mixinClassName.contains(".reloadui.")) {
			return config.reloadui.enabled;
		}
		if (mixinClassName.contains(".ping.")) {
			return config.ping.enabled;
		}
		if (mixinClassName.contains(".extras.")) {
			return config.extras.enabled;
		}
		if (mixinClassName.contains(".imfast.")) {
			if (!config.imfast.enabled) {
				return false;
			}
			boolean iris = net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("iris");
			return switch (name) {
				case "RenderTypeGroupReorderMixin", "ScissorStateMixin" -> config.imfast.enhancedBatching;
				case "MapRendererMixin", "MapTextureManagerMixin", "MapInstanceMixin", "GuiGraphicsExtractorMapMixin", "MapRenderStateMixin" ->
					config.imfast.mapAtlasGeneration;
				case "FontTextureMixin" -> config.imfast.fontAtlasResizing;
				case "TextGlyphLookupMixin", "RenderTypeVertexBuilderInvoker" -> config.imfast.fastTextLookup;
				case "RenderTypesTextSortMixin" -> config.imfast.skipTextTranslucencySorting;
				case "GuiRendererItemBatchMixin", "GuiItemAtlasAnimatedMixin", "GuiItemAtlasAccessor", "DynamicAtlasAllocatorAccessor", "DynamicAtlasSlotAccessor" ->
					config.imfast.batchAnimatedItemUpdates;
				case "GlCommandEncoderFramebufferMixin", "GlSurfacePresentMixin" -> config.imfast.avoidRedundantFramebufferSwitching;
				case "GlCommandEncoderAppleUploadMixin" -> config.imfast.fixSlowBufferUploadOnAppleGpu;
				case "AbstractSignRendererMixin", "SignTextMixin" -> config.imfast.signTextBuffering && !iris;
				case "ShaderManagerConflictMixin" -> config.imfast.resourcePackConflictHandling;
				case "GlDebugInfoMixin" -> config.imfast.printAdditionalErrorInformation;
				case "MinecraftImFastInitMixin", "DebugScreenEntriesMixin", "GameRendererImFastAccessor", "MinecraftFontManagerAccessor" -> true;
				default -> true;
			};
		}
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
}
