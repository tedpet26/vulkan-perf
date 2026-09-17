package dev.vulkanperf.mixin;

import dev.vulkanperf.config.PerfConfig;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class VulkanPerfMixinPlugin implements IMixinConfigPlugin {
	private static boolean modLoaded(String id) {
		return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(id);
	}

	@Override
	public void onLoad(String mixinPackage) {
		PerfConfig.load();
		// Smaller Netty arenas (4 MiB instead of 16 MiB chunks) unless the user pinned one.
		if (System.getProperty("io.netty.allocator.maxOrder") == null) {
			System.setProperty("io.netty.allocator.maxOrder", "9");
		}
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
			if (mixinClassName.contains(".collision.") || mixinClassName.contains(".sleeping.")
					|| mixinClassName.contains(".raycast.") || name.equals("ClassInstanceMultiMapMixin")) {
				// These areas fully replace Lithium; this mod already breaks it, but stay safe
				// against forks under different mod ids.
				if (modLoaded("lithium") || modLoaded("rxithium")) {
					return false;
				}
			}
			return switch (name) {
				case "LevelCollisionMixin" -> config.logic.collisionCache;
				case "HopperBlockEntityMixin", "HopperIdleMixin", "HopperSleepMixin" -> config.logic.hopper;
				case "BrainMixin" -> config.logic.inactiveAi;
				case "ShapesJoinMixin" -> config.logic.voxelShapes;
				case "ShapesJoinIsNotEmptyMixin" -> config.logic.joinIsNotEmptyCache;
				case "ShapesCuboidMatchMixin" -> config.logic.shapesCuboidMatch;
				case "PathNavigationMixin" -> config.logic.pathCache;
				case "WalkNodeEvaluatorPathTypeMixin" -> config.logic.pathTypeCache;
				case "ItemEntityMixin" -> config.logic.itemMerge;
				case "MobAiMixin" -> config.logic.mobAiSkip;
				case "LevelChunkSectionRandomTickMixin" -> config.logic.randomTickSkip;
				case "FluidStateRandomTickMixin" -> config.logic.fluidRandomTickCache;
				case "EntityGetterCollisionsMixin", "ClassInstanceMultiMapMixin", "EntitySectionAccessor",
					"PersistentEntitySectionManagerAccessor", "ServerLevelEntityManagerAccessor" -> config.logic.entityCollisionGroups;
				case "EntitySectionStorageMixin" -> config.logic.entityFastRetrieval;
				case "EntityCollideMixin" -> config.logic.entityFastMovement;
				case "LevelChunkTickerHookMixin", "BlockEntitySleepMixin", "LevelTickBlocksGuardMixin", "SleepingFurnaceMixin",
					"SleepingHopperBlockEntityMixin", "SleepingBrewingStandMixin", "SleepingCampfireMixin", "SleepingCrafterMixin",
					"SleepingShulkerBoxMixin", "SleepingSculkMixin" -> config.logic.sleepingBlockEntities;
				case "InactiveNavigationMixin", "InactiveNavigationLevelMixin", "InactiveNavigationMobMixin" -> config.logic.inactiveNavigations;
				case "LevelChunkTicksMixin" -> config.logic.tickScheduler;
				case "BlockGetterRaycastMixin" -> config.logic.fastRaycast;
				case "ServerExplosionMixin" -> config.logic.explosionOpts;
				case "BlockPosFastMixin", "DirectionFastMixin", "AabbFastMixin", "MthSineMixin" -> config.logic.mathOpts;
				case "ComposterAllocMixin", "EntitySectionIterationMixin", "CompoundTagCopyMixin", "DirectionValuesMixin" -> config.logic.allocOpts;
				case "PoiManagerFastMixin", "PoiSectionFastMixin" -> config.logic.poiOpts;
				case "PathNeighborCacheMixin", "BlockStatePathCacheInitMixin" -> config.logic.pathNeighborCache;
				case "LevelChunkHeightmapMixin" -> config.logic.combinedHeightmap;
				case "RedstoneWireEvaluatorMixin" -> config.logic.redstoneOpts;
				case "FlowingFluidSpreadMixin" -> config.logic.fluidFlowOpts;
				case "GameEventDispatcherMixin" -> config.logic.gameEventOpts;
				case "NoiseBasedChunkGeneratorSettingsMixin" -> config.logic.cachedGenSettings;
				default -> true;
			};
		}
		if (mixinClassName.contains(".chunks.")) {
			return config.chunks.enabled;
		}
		if (mixinClassName.contains(".worldgen.")) {
			return config.chunks.enabled && config.chunks.worldgenOpts;
		}
		if (mixinClassName.contains(".chunksys.")) {
			if (!config.chunks.enabled) {
				return false;
			}
			return switch (name) {
				case "MinecraftServerSchedulingMixin" -> config.chunks.midTickScheduling;
				case "RegionFileStorageCacheMixin" -> config.chunks.asyncIoDeepened;
				case "OptionsViewDistanceMixin" -> config.chunks.clientViewDistanceUncap;
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
		if (mixinClassName.contains(".network.")) {
			// Krypton rewrites the same frame/codec paths; defer to the real mod when present.
			return config.network.enabled && !modLoaded("krypton");
		}
		if (mixinClassName.contains(".moreculling.")) {
			return config.moreculling.enabled && !modLoaded("moreculling");
		}
		if (mixinClassName.contains(".mfix.")) {
			if (!config.mfix.enabled || modLoaded("modernfix")) {
				return false;
			}
			return switch (name) {
				case "FilePackResourcesIndexMixin" -> config.mfix.zipIndex;
				case "SoundBufferLibraryExpiryMixin" -> config.mfix.dynamicSounds;
				case "StructureTemplateManagerSoftCacheMixin" -> config.mfix.dynamicStructures;
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
			// Particle Core / AsyncParticles rewrite the same extract paths.
			return config.particles.enabled && !modLoaded("particle_core") && !modLoaded("asyncparticles");
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
				case "GuiRenderStateIntersectionMixin", "GuiRenderStateNodeMixin" -> config.imfast.guiIntersectionFastPath;
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
