package dev.vulkanperf.client.config;

import dev.vulkanperf.config.PerfConfig;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.api.config.structure.BooleanOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.IntegerOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionGroupBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Full Sodium options-screen integration: every module's settings are exposed here.
 * The "Modules" page holds all master toggles; sub-options gray out when their
 * module (or parent flag) is off, evaluated via {@code ConfigState#readBooleanOption}
 * on the registered parent option ids. All bindings read/write {@link PerfConfig}'s
 * live singleton (never a cached copy — config reloads replace the instance),
 * persisted to disk by {@link #afterSave()}.
 */
public final class VulkanPerfSodiumConfig implements ConfigEntryPoint, StorageEventHandler {
	private static final String RESTART = "Requires a game restart to take effect.";

	private static Identifier id(String path) {
		// Identifier paths only allow [a-z0-9/._-]; normalize our camelCase keys.
		return Identifier.fromNamespaceAndPath("vulkanperf", path.toLowerCase(java.util.Locale.ROOT));
	}

	@Override
	public void registerConfigLate(ConfigBuilder builder) {
		builder.registerOwnModOptions()
			.setIcon(Identifier.fromNamespaceAndPath("minecraft", "textures/item/redstone.png"))
			.addPage(modulesPage(builder))
			.addPage(logicPage(builder))
			.addPage(chunksPage(builder))
			.addPage(networkPage(builder))
			.addPage(memoryPage(builder))
			.addPage(cullingPage(builder))
			.addPage(clientPage(builder))
			.addPage(imfastPage(builder))
			.addPage(extrasAnimationPage(builder))
			.addPage(extrasParticlesPage(builder))
			.addPage(extrasDetailPage(builder))
			.addPage(extrasRenderPage(builder))
			.addPage(extrasExtraPage(builder));
	}

	// ------------------------------------------------------------------
	// Pages
	// ------------------------------------------------------------------

	private OptionPageBuilder modulesPage(ConfigBuilder b) {
		return b.createOptionPage()
			.setName(Component.literal("Modules"))
			.addOptionGroup(group(b, "Master switches for each optimization module")
				.addOption(flag(b, "module.logic", "Game logic", "Optimizations for collision, hoppers, AI, voxel shapes and pathfinding. " + RESTART,
					v -> PerfConfig.get().logic.enabled = v, () -> PerfConfig.get().logic.enabled, true, OptionImpact.MEDIUM))
				.addOption(flag(b, "module.chunks", "Chunk system", "Extra worldgen workers and mid-tick chunk task scheduling. " + RESTART,
					v -> PerfConfig.get().chunks.enabled = v, () -> PerfConfig.get().chunks.enabled, true, OptionImpact.MEDIUM))
				.addOption(flag(b, "module.packets", "Packet limits", "Redirect vanilla network size limits to the values on the Network page. On by default: it mirrors vanilla until a limit is raised.",
					v -> PerfConfig.get().packets.enabled = v, () -> PerfConfig.get().packets.enabled, true, OptionImpact.LOW))
				.addOption(flag(b, "module.memory", "Memory", "Shape interning, block state table deduplication and data component sharing. " + RESTART,
					v -> PerfConfig.get().memory.enabled = v, () -> PerfConfig.get().memory.enabled, true, OptionImpact.MEDIUM))
				.addOption(flag(b, "module.logging", "Log cleanup", "Filter repetitive log spam.",
					v -> PerfConfig.get().logging.enabled = v, () -> PerfConfig.get().logging.enabled, true, OptionImpact.LOW))
				.addOption(flag(b, "module.power", "Power saving", "Reduce FPS and mute the game while unfocused or hidden.",
					v -> PerfConfig.get().power.enabled = v, () -> PerfConfig.get().power.enabled, true, OptionImpact.LOW))
				.addOption(flag(b, "module.particles", "Particle culling", "Frustum-cull particle rendering. " + RESTART,
					v -> PerfConfig.get().particles.enabled = v, () -> PerfConfig.get().particles.enabled, true, OptionImpact.LOW))
				.addOption(flag(b, "module.culling", "Entity culling", "Skip rendering entities and block entities that are occluded or far away. " + RESTART,
					v -> PerfConfig.get().culling.enabled = v, () -> PerfConfig.get().culling.enabled, true, OptionImpact.MEDIUM))
				.addOption(flag(b, "module.clientcache", "Client caches", "Cache toast/sky visibility lookups on the client. " + RESTART,
					v -> PerfConfig.get().clientcache.enabled = v, () -> PerfConfig.get().clientcache.enabled, true, OptionImpact.LOW))
				.addOption(flag(b, "module.hudspread", "HUD spreading", "Spread HUD updates across multiple frames to reduce frame spikes.",
					v -> PerfConfig.get().hudspread.enabled = v, () -> PerfConfig.get().hudspread.enabled, true, OptionImpact.LOW))
				.addOption(flag(b, "module.reloadui", "Reload UI skip", "Skip the resource reload loading screen after the first one.",
					v -> PerfConfig.get().reloadui.enabled = v, () -> PerfConfig.get().reloadui.enabled, true, OptionImpact.LOW))
				.addOption(flag(b, "module.ping", "Ping tweaks", "Ping display optimizations. " + RESTART,
					v -> PerfConfig.get().ping.enabled = v, () -> PerfConfig.get().ping.enabled, true, OptionImpact.LOW))
				.addOption(flag(b, "module.extras", "Extras", "Sodium-extra-style visual toggles: animations, weather, sky, particles and detail rendering.",
					v -> PerfConfig.get().extras.enabled = v, () -> PerfConfig.get().extras.enabled, true, OptionImpact.VARIES))
				.addOption(flag(b, "module.imfast", "Text & batching", "ImmediatelyFast-class GUI/text batching, atlas packing and GL fixes. " + RESTART,
					v -> PerfConfig.get().imfast.enabled = v, () -> PerfConfig.get().imfast.enabled, true, OptionImpact.MEDIUM)));
	}

	private OptionPageBuilder logicPage(ConfigBuilder b) {
		return b.createOptionPage()
			.setName(Component.literal("Logic"))
			.addOptionGroup(group(b, "Hot-path game logic optimizations")
				.addOption(gatedFlag(b, "logic.collisionCache", "Collision cache", "Cache block collision shape lookups. " + RESTART,
					v -> PerfConfig.get().logic.collisionCache = v, () -> PerfConfig.get().logic.collisionCache, true, "module.logic"))
				.addOption(gatedFlag(b, "logic.hopper", "Hopper optimizations", "Faster hopper transfer lookups. " + RESTART,
					v -> PerfConfig.get().logic.hopper = v, () -> PerfConfig.get().logic.hopper, true, "module.logic"))
				.addOption(gatedFlag(b, "logic.inactiveAi", "Inactive AI skip", "Skip AI ticking for distant or inactive mobs. " + RESTART,
					v -> PerfConfig.get().logic.inactiveAi = v, () -> PerfConfig.get().logic.inactiveAi, true, "module.logic"))
				.addOption(gatedFlag(b, "logic.voxelShapes", "Voxel shape optimizations", "Optimize voxel shape merging and lookups. " + RESTART,
					v -> PerfConfig.get().logic.voxelShapes = v, () -> PerfConfig.get().logic.voxelShapes, true, "module.logic"))
				.addOption(gatedFlag(b, "logic.pathCache", "Pathfinding cache", "Cache mob pathfinding nodes. " + RESTART,
					v -> PerfConfig.get().logic.pathCache = v, () -> PerfConfig.get().logic.pathCache, true, "module.logic"))
				.addOption(gatedFlag(b, "logic.itemMerge", "Item merge", "Optimize nearby-item merging checks. " + RESTART,
					v -> PerfConfig.get().logic.itemMerge = v, () -> PerfConfig.get().logic.itemMerge, true, "module.logic"))
				.addOption(gatedFlag(b, "logic.mobAiSkip", "Mob AI skip", "Skip redundant mob AI sub-ticks. " + RESTART,
					v -> PerfConfig.get().logic.mobAiSkip = v, () -> PerfConfig.get().logic.mobAiSkip, true, "module.logic")))
			.addOptionGroup(group(b, "Tier S expansion")
				.addOption(gatedFlag(b, "logic.hopperSleep", "Hopper sleep", "Extend the hopper idle cooldown; wake it when its inventory changes. " + RESTART,
					v -> PerfConfig.get().logic.hopperSleep = v, () -> PerfConfig.get().logic.hopperSleep, true, "module.logic"))
				.addOption(gatedFlag(b, "logic.joinIsNotEmptyCache", "Shape join cache", "Cache Shapes#joinIsNotEmpty results for identical shape pairs (occlusion, collision checks). " + RESTART,
					v -> PerfConfig.get().logic.joinIsNotEmptyCache = v, () -> PerfConfig.get().logic.joinIsNotEmptyCache, true, "module.logic"))
				.addOption(gatedFlag(b, "logic.pathTypeCache", "Path type cache", "Cache WalkNodeEvaluator#getPathTypeFromState per BlockState, shared across mobs. " + RESTART,
					v -> PerfConfig.get().logic.pathTypeCache = v, () -> PerfConfig.get().logic.pathTypeCache, true, "module.logic"))
				.addOption(gatedFlag(b, "logic.randomTickSkip", "Random tick skip", "Skip random-tick iteration for all-air sections. " + RESTART,
					v -> PerfConfig.get().logic.randomTickSkip = v, () -> PerfConfig.get().logic.randomTickSkip, true, "module.logic")));
	}

	private OptionPageBuilder chunksPage(ConfigBuilder b) {
		int cores = Runtime.getRuntime().availableProcessors();
		return b.createOptionPage()
			.setName(Component.literal("Chunks"))
			.addOptionGroup(group(b, "Worker threads")
				.addOption(gatedInt(b, "chunks.worldgenThreads", "Worldgen threads", "Threads used for chunk generation. " + RESTART,
					v -> PerfConfig.get().chunks.worldgenThreads = v, () -> PerfConfig.get().chunks.worldgenThreads, Math.max(1, cores - 1), 1, cores + 4, 1, "module.chunks")
					.setValueFormatter(v -> Component.literal(v + " threads"))
					.setImpact(OptionImpact.VARIES))
				.addOption(gatedInt(b, "chunks.serializeThreads", "Serialize threads", "Threads used for chunk serialization. " + RESTART,
					v -> PerfConfig.get().chunks.serializeThreads = v, () -> PerfConfig.get().chunks.serializeThreads, 2, 1, 16, 1, "module.chunks")
					.setValueFormatter(v -> Component.literal(v + " threads")))
				.addOption(gatedInt(b, "chunks.ioThreads", "IO threads", "Threads used for chunk disk IO. " + RESTART,
					v -> PerfConfig.get().chunks.ioThreads = v, () -> PerfConfig.get().chunks.ioThreads, 2, 1, 16, 1, "module.chunks")
					.setValueFormatter(v -> Component.literal(v + " threads"))))
			.addOptionGroup(group(b, "Scheduling")
				.addOption(gatedFlag(b, "chunks.midTickScheduling", "Mid-tick scheduling", "Drain queued chunk tasks during each tick instead of only between ticks. " + RESTART,
					v -> PerfConfig.get().chunks.midTickScheduling = v, () -> PerfConfig.get().chunks.midTickScheduling, true, "module.chunks"))
				.addOption(gatedInt(b, "chunks.midTickInterval", "Mid-tick drain interval", "How often (in milliseconds) queued chunk tasks are drained mid-tick.",
					v -> PerfConfig.get().chunks.midTickIntervalNanos = v * 1_000_000L, () -> (int) (PerfConfig.get().chunks.midTickIntervalNanos / 1_000_000L), 2, 0, 50, 1, "chunks.midTickScheduling")
					.setValueFormatter(v -> Component.literal(v + " ms"))))
			.addOptionGroup(group(b, "Storage")
				.addOption(gatedFlag(b, "chunks.asyncIoDeepened", "Deep async IO", "Larger region file caches and executor rewiring. " + RESTART,
					v -> PerfConfig.get().chunks.asyncIoDeepened = v, () -> PerfConfig.get().chunks.asyncIoDeepened, true, "module.chunks"))
				.addOption(gatedInt(b, "chunks.regionFileCacheSize", "Region file cache size", "Number of open region files kept cached.",
					v -> PerfConfig.get().chunks.regionFileCacheSize = v, () -> PerfConfig.get().chunks.regionFileCacheSize, 256, 16, 1024, 16, "chunks.asyncIoDeepened")))
			.addOptionGroup(group(b, "Client")
				.addOption(gatedFlag(b, "chunks.clientViewDistanceUncap", "Uncap render distance", "Raise the render distance slider cap beyond vanilla's 32. " + RESTART,
					v -> PerfConfig.get().chunks.clientViewDistanceUncap = v, () -> PerfConfig.get().chunks.clientViewDistanceUncap, true, "module.chunks"))
				.addOption(gatedInt(b, "chunks.clientMaxViewDistance", "Max render distance", "New slider cap while render distance is uncapped.",
					v -> PerfConfig.get().chunks.clientMaxViewDistance = v, () -> PerfConfig.get().chunks.clientMaxViewDistance, 48, 32, 128, 2, "chunks.clientViewDistanceUncap")
					.setImpact(OptionImpact.HIGH)))
			.addOptionGroup(group(b, "Experimental (not implemented)")
				.addOption(disabledFlag(b, "chunks.nativesMath", "Native math", "Not implemented yet."))
				.addOption(disabledFlag(b, "chunks.openclAccel", "OpenCL acceleration", "Not implemented yet."))
				.addOption(disabledFlag(b, "chunks.rewriteChunkSystem", "Chunk system rewrite", "Deferred full ticket/holder rewrite. Not implemented yet.")));
	}

	private OptionPageBuilder networkPage(ConfigBuilder b) {
		return b.createOptionPage()
			.setName(Component.literal("Network"))
			.addOptionGroup(group(b, "Vanilla limits (redirected while the module is on)")
				.addOption(gatedInt(b, "packets.nbtQuota", "NBT quota", "Maximum size of NBT read from the network. Vanilla default: 2 MiB.",
					v -> PerfConfig.get().packets.nbtQuota = v, () -> PerfConfig.get().packets.nbtQuota, 2_097_152, 1_048_576, 67_108_864, 1_048_576, "module.packets")
					.setValueFormatter(v -> Component.literal((v / 1_048_576) + " MiB")))
				.addOption(gatedInt(b, "packets.stringSize", "String size", "Maximum length of strings read/written over the network. Vanilla default: 32767.",
					v -> PerfConfig.get().packets.stringSize = v, () -> PerfConfig.get().packets.stringSize, 32_767, 256, 1_048_576, 256, "module.packets")
					.setValueFormatter(v -> Component.literal(v + " chars")))
				.addOption(gatedInt(b, "packets.compression", "Compression cap", "Maximum decompressed packet size accepted. Vanilla default since 26.3: 8 MiB.",
					v -> PerfConfig.get().packets.compression = v, () -> PerfConfig.get().packets.compression, 8_388_608, 1_048_576, 67_108_864, 1_048_576, "module.packets")
					.setValueFormatter(v -> Component.literal((v / 1_048_576) + " MiB"))));
	}

	private OptionPageBuilder memoryPage(ConfigBuilder b) {
		return b.createOptionPage()
			.setName(Component.literal("Memory"))
			.addOptionGroup(group(b, "Allocation & deduplication")
				.addOption(gatedFlag(b, "memory.internShapes", "Intern shapes", "Reuse identical Shapes#join results. " + RESTART,
					v -> PerfConfig.get().memory.internShapes = v, () -> PerfConfig.get().memory.internShapes, true, "module.memory"))
				.addOption(gatedFlag(b, "memory.fastMapNeighborLookup", "Shared neighbor tables", "One shared neighbor table per block instead of a per-state array (FerriteCore-style). " + RESTART,
					v -> PerfConfig.get().memory.fastMapNeighborLookup = v, () -> PerfConfig.get().memory.fastMapNeighborLookup, true, "module.memory"))
				.addOption(gatedFlag(b, "memory.fastMapPropertyMap", "Packed property values", "Drop the per-state propertyValues array and read from the neighbor table. Needs shared neighbor tables. " + RESTART,
					v -> PerfConfig.get().memory.fastMapPropertyMap = v, () -> PerfConfig.get().memory.fastMapPropertyMap, true, "memory.fastMapNeighborLookup", "module.memory"))
				.addOption(gatedFlag(b, "memory.blockStateCacheDedup", "Block state cache dedup", "Intern structurally equal collision shapes / face-sturdy tables across block states. " + RESTART,
					v -> PerfConfig.get().memory.blockStateCacheDedup = v, () -> PerfConfig.get().memory.blockStateCacheDedup, true, "module.memory"))
				.addOption(gatedFlag(b, "memory.dataComponentPatchSharing", "Share empty component patches", "Share one immutable empty patch map across item stacks without component overrides. " + RESTART,
					v -> PerfConfig.get().memory.dataComponentPatchSharing = v, () -> PerfConfig.get().memory.dataComponentPatchSharing, true, "module.memory")))
			.addOptionGroup(group(b, "Opt-in / experimental")
				.addOption(gatedFlag(b, "memory.compactFastMap", "Compact FastMap index", "Denser FastMap index (smaller table, integer division). Default off. " + RESTART,
					v -> PerfConfig.get().memory.compactFastMap = v, () -> PerfConfig.get().memory.compactFastMap, false, "module.memory"))
				.addOption(gatedFlag(b, "memory.smallThreadDetector", "Small threading detector", "Replace PalettedContainer's ThreadingDetector object with a single byte. Default off. " + RESTART,
					v -> PerfConfig.get().memory.smallThreadDetector = v, () -> PerfConfig.get().memory.smallThreadDetector, false, "module.memory")));
	}

	private OptionPageBuilder cullingPage(ConfigBuilder b) {
		return b.createOptionPage()
			.setName(Component.literal("Culling"))
			.addOptionGroup(group(b, "Entity culling")
				.addOption(gatedFlag(b, "culling.entities", "Cull entities", "Skip rendering occluded/off-screen entities. " + RESTART,
					v -> PerfConfig.get().culling.entities = v, () -> PerfConfig.get().culling.entities, true, "module.culling"))
				.addOption(gatedInt(b, "culling.entityCacheTicks", "Visibility cache ticks", "How long an entity's visibility result is reused.",
					v -> PerfConfig.get().culling.entityCacheTicks = v, () -> PerfConfig.get().culling.entityCacheTicks, 10, 1, 40, 1, "culling.entities"))
				.addOption(gatedInt(b, "culling.entityMaxDistance", "Max cull distance", "Entities beyond this distance are always culled.",
					v -> PerfConfig.get().culling.entityMaxDistance = v, () -> (int) PerfConfig.get().culling.entityMaxDistance, 128, 32, 512, 16, "culling.entities")
					.setValueFormatter(v -> Component.literal(v + " blocks"))))
			.addOptionGroup(group(b, "Block entities")
				.addOption(gatedFlag(b, "culling.blockEntities", "Cull block entities", "Skip rendering occluded/off-screen block entities (chests, signs...). " + RESTART,
					v -> PerfConfig.get().culling.blockEntities = v, () -> PerfConfig.get().culling.blockEntities, true, "module.culling")))
			.addOptionGroup(group(b, "Particles")
				.addOption(gatedFlag(b, "particles.frustumCull", "Frustum cull particles", "Skip particles outside the camera view. " + RESTART,
					v -> PerfConfig.get().particles.frustumCull = v, () -> PerfConfig.get().particles.frustumCull, true, "module.particles")));
	}

	private OptionPageBuilder clientPage(ConfigBuilder b) {
		return b.createOptionPage()
			.setName(Component.literal("Client"))
			.addOptionGroup(group(b, "Power saving")
				.addOption(gatedInt(b, "power.unfocusedFps", "Unfocused FPS", "FPS limit while the window is not focused.",
					v -> PerfConfig.get().power.unfocusedFps = v, () -> PerfConfig.get().power.unfocusedFps, 10, 1, 260, 1, "module.power")
					.setValueFormatter(v -> Component.literal(v + " FPS")))
				.addOption(gatedInt(b, "power.hiddenFps", "Hidden FPS", "FPS limit while the window is minimized or hidden.",
					v -> PerfConfig.get().power.hiddenFps = v, () -> PerfConfig.get().power.hiddenFps, 1, 1, 260, 1, "module.power")
					.setValueFormatter(v -> Component.literal(v + " FPS")))
				.addOption(gatedFlag(b, "power.muteUnfocused", "Mute when unfocused", "Mute game audio while the window is not focused.",
					v -> PerfConfig.get().power.muteUnfocused = v, () -> PerfConfig.get().power.muteUnfocused, true, "module.power")))
			.addOptionGroup(group(b, "HUD")
				.addOption(gatedInt(b, "hudspread.spreadFrames", "HUD update spread", "Number of frames over which expensive HUD updates are spread.",
					v -> PerfConfig.get().hudspread.spreadFrames = v, () -> PerfConfig.get().hudspread.spreadFrames, 2, 1, 8, 1, "module.hudspread")))
			.addOptionGroup(group(b, "Resource reload UI")
				.addOption(gatedFlag(b, "reloadui.skipAfterFirst", "Skip after first screen", "Skip the loading screen on subsequent resource reloads.",
					v -> PerfConfig.get().reloadui.skipAfterFirst = v, () -> PerfConfig.get().reloadui.skipAfterFirst, true, "module.reloadui")))
			.addOptionGroup(group(b, "Client caches")
				.addOption(gatedFlag(b, "clientcache.toasts", "Cache toast visibility", "Cache toast (advancement popup) visibility lookups. " + RESTART,
					v -> PerfConfig.get().clientcache.toasts = v, () -> PerfConfig.get().clientcache.toasts, true, "module.clientcache"))
				.addOption(gatedFlag(b, "clientcache.sky", "Cache sky visibility", "Cache sky rendering visibility lookups. " + RESTART,
					v -> PerfConfig.get().clientcache.sky = v, () -> PerfConfig.get().clientcache.sky, true, "module.clientcache")));
	}

	private OptionPageBuilder imfastPage(ConfigBuilder b) {
		return b.createOptionPage()
			.setName(Component.literal("Text & Batching"))
			.addOptionGroup(group(b, "Batching")
				.addOption(gatedFlag(b, "imfast.enhancedBatching", "Enhanced batching", "Always allow draw-call reordering/merging in the feature renderer group. " + RESTART,
					v -> PerfConfig.get().imfast.enhancedBatching = v, () -> PerfConfig.get().imfast.enhancedBatching, true, "module.imfast"))
				.addOption(gatedFlag(b, "imfast.batchAnimatedItemUpdates", "Batch animated items", "Route animated item icons to a dedicated small atlas so they don't invalidate the main GUI atlas every frame. " + RESTART,
					v -> PerfConfig.get().imfast.batchAnimatedItemUpdates = v, () -> PerfConfig.get().imfast.batchAnimatedItemUpdates, true, "module.imfast"))
				.addOption(gatedFlag(b, "imfast.fastTextLookup", "Fast text lookup", "Cache the last resolved VertexConsumer per glyph renderer. " + RESTART,
					v -> PerfConfig.get().imfast.fastTextLookup = v, () -> PerfConfig.get().imfast.fastTextLookup, true, "module.imfast"))
				.addOption(gatedFlag(b, "imfast.skipTextTranslucencySorting", "Skip text sorting", "Disable vertex sorting for polygon-offset/see-through text render types. " + RESTART,
					v -> PerfConfig.get().imfast.skipTextTranslucencySorting = v, () -> PerfConfig.get().imfast.skipTextTranslucencySorting, true, "module.imfast")))
			.addOptionGroup(group(b, "Atlases")
				.addOption(gatedFlag(b, "imfast.mapAtlasGeneration", "Map atlases", "Pack map textures into shared GPU atlases instead of one texture per map. " + RESTART,
					v -> PerfConfig.get().imfast.mapAtlasGeneration = v, () -> PerfConfig.get().imfast.mapAtlasGeneration, true, "module.imfast"))
				.addOption(gatedInt(b, "imfast.mapAtlasSize", "Map atlas size", "Edge length of each packed map atlas sheet (snapped to a power of two).",
					v -> PerfConfig.get().imfast.mapAtlasSize = roundPow2(v, 512, 8192), () -> PerfConfig.get().imfast.mapAtlasSize, 2048, 512, 8192, 512, "imfast.mapAtlasGeneration"))
				.addOption(gatedFlag(b, "imfast.fontAtlasResizing", "Font atlas resizing", "Grow the glyph atlas beyond vanilla's 256x256 to reduce texture switches. " + RESTART,
					v -> PerfConfig.get().imfast.fontAtlasResizing = v, () -> PerfConfig.get().imfast.fontAtlasResizing, true, "module.imfast"))
				.addOption(gatedInt(b, "imfast.fontAtlasSize", "Font atlas size", "Edge length of the glyph atlas (snapped to a power of two).",
					v -> PerfConfig.get().imfast.fontAtlasSize = roundPow2(v, 256, 4096), () -> PerfConfig.get().imfast.fontAtlasSize, 1024, 256, 4096, 256, "imfast.fontAtlasResizing")))
			.addOptionGroup(group(b, "GL & compatibility")
				.addOption(gatedFlag(b, "imfast.avoidRedundantFramebufferSwitching", "Skip redundant FBO switches", "GL backend only: skip redundant framebuffer unbinds between passes / before swap. " + RESTART,
					v -> PerfConfig.get().imfast.avoidRedundantFramebufferSwitching = v, () -> PerfConfig.get().imfast.avoidRedundantFramebufferSwitching, true, "module.imfast"))
				.addOption(gatedFlag(b, "imfast.fixSlowBufferUploadOnAppleGpu", "Apple GPU buffer upload fix", "GL + Apple GPU only: use bufferData instead of bufferSubData for full-buffer uploads. " + RESTART,
					v -> PerfConfig.get().imfast.fixSlowBufferUploadOnAppleGpu = v, () -> PerfConfig.get().imfast.fixSlowBufferUploadOnAppleGpu, true, "module.imfast"))
				.addOption(gatedFlag(b, "imfast.resourcePackConflictHandling", "Resource pack conflict handling", "Temporarily disable font/map atlas options when a resource pack replaces core text shaders.",
					v -> PerfConfig.get().imfast.resourcePackConflictHandling = v, () -> PerfConfig.get().imfast.resourcePackConflictHandling, true, "module.imfast"))
				.addOption(gatedFlag(b, "imfast.printAdditionalErrorInformation", "Verbose GL debug", "GL backend only: print a stack trace with OpenGL debug callback messages (noisy).",
					v -> PerfConfig.get().imfast.printAdditionalErrorInformation = v, () -> PerfConfig.get().imfast.printAdditionalErrorInformation, false, "module.imfast")))
			.addOptionGroup(group(b, "Experimental")
				.addOption(gatedFlag(b, "imfast.signTextBuffering", "Sign text buffering", "Rasterize static sign text into a shared atlas (skipped when Iris is loaded). " + RESTART,
					v -> PerfConfig.get().imfast.signTextBuffering = v, () -> PerfConfig.get().imfast.signTextBuffering, false, "module.imfast"))
				.addOption(gatedInt(b, "imfast.signAtlasSize", "Sign atlas size", "Edge length of the experimental sign-text atlas (snapped to a power of two).",
					v -> PerfConfig.get().imfast.signAtlasSize = roundPow2(v, 1024, 8192), () -> PerfConfig.get().imfast.signAtlasSize, 4096, 1024, 8192, 512, "imfast.signTextBuffering")));
	}

	// ------------------------------------------------------------------
	// Extras pages (Sodium-extra-style visual toggles)
	// ------------------------------------------------------------------

	private OptionPageBuilder extrasAnimationPage(ConfigBuilder b) {
		return b.createOptionPage()
			.setName(Component.literal("Animation"))
			.addOptionGroup(group(b, "Animated textures")
				.addOption(extrasFlag(b, "animations_all", "Animations", "Master toggle for animated textures", v -> extras().animation = v, () -> extras().animation, true))
				.addOption(extrasFlag(b, "animate_water", "Water", "Animated water textures", v -> extras().animateWater = v, () -> extras().animateWater, true, "animations_all"))
				.addOption(extrasFlag(b, "animate_lava", "Lava", "Animated lava textures", v -> extras().animateLava = v, () -> extras().animateLava, true, "animations_all"))
				.addOption(extrasFlag(b, "animate_fire", "Fire", "Animated fire textures", v -> extras().animateFire = v, () -> extras().animateFire, true, "animations_all"))
				.addOption(extrasFlag(b, "animate_portal", "Portal", "Animated portal textures", v -> extras().animatePortal = v, () -> extras().animatePortal, true, "animations_all"))
				.addOption(extrasFlag(b, "block_animations", "Block animations", "Other animated block textures", v -> extras().animateBlocks = v, () -> extras().animateBlocks, true, "animations_all"))
				.addOption(extrasFlag(b, "animate_sculk", "Sculk", "Animated sculk textures", v -> extras().animateSculk = v, () -> extras().animateSculk, true, "animations_all")));
	}

	private OptionPageBuilder extrasParticlesPage(ConfigBuilder b) {
		return b.createOptionPage()
			.setName(Component.literal("Particles"))
			.addOptionGroup(group(b, "Particle rendering")
				.addOption(extrasFlag(b, "particles_all", "Particles", "Master toggle for particles", v -> extras().particles = v, () -> extras().particles, true))
				.addOption(extrasFlag(b, "weather_particles", "Weather particles", "Rain splash and weather particles", v -> extras().weatherParticles = v, () -> extras().weatherParticles, true, "particles_all"))
				.addOption(extrasFlag(b, "rain_splash", "Rain splash", "Rain splash particles on the ground", v -> extras().rainSplash = v, () -> extras().rainSplash, true, "particles_all"))
				.addOption(extrasFlag(b, "block_break", "Block break", "Particles when a block is broken", v -> extras().blockBreak = v, () -> extras().blockBreak, true, "particles_all"))
				.addOption(extrasFlag(b, "block_breaking", "Block cracking", "Particles while mining a block", v -> extras().blockBreaking = v, () -> extras().blockBreaking, true, "particles_all")));
	}

	private OptionPageBuilder extrasDetailPage(ConfigBuilder b) {
		return b.createOptionPage()
			.setName(Component.literal("Detail"))
			.addOptionGroup(group(b, "Environment detail")
				.addOption(extrasFlag(b, "sky", "Sky", "Render the sky disc", v -> extras().sky = v, () -> extras().sky, true))
				.addOption(extrasFlag(b, "sun", "Sun", "Render the sun", v -> extras().sun = v, () -> extras().sun, true))
				.addOption(extrasFlag(b, "moon", "Moon", "Render the moon", v -> extras().moon = v, () -> extras().moon, true))
				.addOption(extrasFlag(b, "stars", "Stars", "Render stars", v -> extras().stars = v, () -> extras().stars, true))
				.addOption(extrasFlag(b, "rain_snow", "Rain & snow", "Render rain and snow weather", v -> extras().rainSnow = v, () -> extras().rainSnow, true))
				.addOption(extrasFlag(b, "biome_colors", "Biome colors", "Use biome-tinted grass and foliage", v -> extras().biomeColors = v, () -> extras().biomeColors, true))
				.addOption(extrasFlag(b, "sky_colors", "Sky colors", "Use biome sky colors", v -> extras().skyColors = v, () -> extras().skyColors, true)));
	}

	private OptionPageBuilder extrasRenderPage(ConfigBuilder b) {
		return b.createOptionPage()
			.setName(Component.literal("Render"))
			.addOptionGroup(group(b, "Render toggles")
				.addOption(extrasFlag(b, "fog", "Fog", "Terrain fog and Sodium fog culling", v -> extras().fog = v, () -> extras().fog, true))
				.addOption(extrasFlag(b, "light_updates", "Light updates", "Process lighting engine updates", v -> extras().lightUpdates = v, () -> extras().lightUpdates, true))
				.addOption(extrasFlag(b, "item_frames", "Item frames", "Render item frames", v -> extras().itemFrames = v, () -> extras().itemFrames, true))
				.addOption(extrasFlag(b, "armor_stands", "Armor stands", "Render armor stands", v -> extras().armorStands = v, () -> extras().armorStands, true))
				.addOption(extrasFlag(b, "paintings", "Paintings", "Render paintings", v -> extras().paintings = v, () -> extras().paintings, true))
				.addOption(extrasFlag(b, "pistons", "Piston animations", "Render moving pistons", v -> extras().pistons = v, () -> extras().pistons, true))
				.addOption(extrasFlag(b, "beacon_beams", "Beacon beams", "Render beacon beams", v -> extras().beaconBeams = v, () -> extras().beaconBeams, true))
				.addOption(extrasFlag(b, "enchanting_table_book", "Enchanting table book", "Render the enchanting table book", v -> extras().enchantingTableBook = v, () -> extras().enchantingTableBook, true))
				.addOption(extrasFlag(b, "nametags", "Player nametags", "Render player nametags", v -> extras().nametags = v, () -> extras().nametags, true))
				.addOption(extrasFlag(b, "item_frame_nametags", "Item frame nametags", "Render item frame nametags", v -> extras().itemFrameNametags = v, () -> extras().itemFrameNametags, true)));
	}

	private OptionPageBuilder extrasExtraPage(ConfigBuilder b) {
		return b.createOptionPage()
			.setName(Component.literal("Extra"))
			.addOptionGroup(group(b, "HUD overlays")
				.addOption(extrasFlag(b, "show_fps", "FPS overlay", "Show FPS in the corner of the HUD", v -> extras().overlayFps = v, () -> extras().overlayFps, true))
				.addOption(extrasFlag(b, "show_fps_extended", "FPS overlay extended", "Show average and low FPS", v -> extras().overlayFpsExtended = v, () -> extras().overlayFpsExtended, false, "show_fps"))
				.addOption(extrasFlag(b, "show_coordinates", "Coordinate overlay", "Show player coordinates on the HUD", v -> extras().overlayCoords = v, () -> extras().overlayCoords, false)))
			.addOptionGroup(group(b, "Misc")
				.addOption(extrasFlag(b, "toasts", "Toasts", "Show advancement and recipe toasts", v -> extras().toasts = v, () -> extras().toasts, true))
				.addOption(extrasInt(b, "overlay_update_ms", "Overlay update rate", "How often HUD overlay text is refreshed.",
					v -> extras().overlayUpdateMs = v, () -> extras().overlayUpdateMs, 500, 100, 2000, 100, "show_fps")
					.setValueFormatter(v -> Component.literal(v + " ms"))));
	}

	// ------------------------------------------------------------------
	// Option construction helpers
	// ------------------------------------------------------------------

	private OptionGroupBuilder group(ConfigBuilder b, String name) {
		return b.createOptionGroup().setName(Component.literal(name));
	}

	private BooleanOptionBuilder flag(ConfigBuilder b, String key, String name, String tooltip,
		Consumer<Boolean> setter, Supplier<Boolean> getter, boolean def) {
		return b.createBooleanOption(id(key))
			.setName(Component.literal(name))
			.setTooltip(Component.literal(tooltip))
			.setStorageHandler(this)
			.setBinding(setter, getter)
			.setDefaultValue(def);
	}

	private BooleanOptionBuilder flag(ConfigBuilder b, String key, String name, String tooltip,
		Consumer<Boolean> setter, Supplier<Boolean> getter, boolean def, OptionImpact impact) {
		return flag(b, key, name, tooltip, setter, getter, def).setImpact(impact);
	}

	/**
	 * Boolean option gated on the named parent option(s) (registered boolean option
	 * keys, e.g. "module.logic"): grays out unless every parent reads as on.
	 */
	private BooleanOptionBuilder gatedFlag(ConfigBuilder b, String key, String name, String tooltip,
		Consumer<Boolean> setter, Supplier<Boolean> getter, boolean def, String... deps) {
		Identifier[] depIds = ids(deps);
		return flag(b, key, name, tooltip, setter, getter, def).setEnabledProvider(
			state -> allDepsOn(state, depIds), depIds);
	}

	private BooleanOptionBuilder disabledFlag(ConfigBuilder b, String key, String name, String tooltip) {
		return flag(b, key, name, tooltip, v -> {}, () -> false, false).setEnabled(false);
	}

	/** Extras option: also requires the extras module master and the given parent flag(s). */
	private BooleanOptionBuilder extrasFlag(ConfigBuilder b, String key, String name, String tooltip,
		Consumer<Boolean> setter, Supplier<Boolean> getter, boolean def, String... deps) {
		String[] all = new String[deps.length + 1];
		all[0] = "module.extras";
		System.arraycopy(deps, 0, all, 1, deps.length);
		return gatedFlag(b, key, name, tooltip, setter, getter, def, all);
	}

	private IntegerOptionBuilder gatedInt(ConfigBuilder b, String key, String name, String tooltip,
		Consumer<Integer> setter, Supplier<Integer> getter, int def, int min, int max, int step, String... deps) {
		Identifier[] depIds = ids(deps);
		return b.createIntegerOption(id(key))
			.setName(Component.literal(name))
			.setTooltip(Component.literal(tooltip))
			.setStorageHandler(this)
			.setBinding(setter, getter)
			.setDefaultValue(def)
			.setRange(min, max, step)
			// Sodium rejects int options without a formatter; sites with nicer
			// units override this default afterwards.
			.setValueFormatter(v -> Component.literal(String.valueOf(v)))
			.setEnabledProvider(state -> allDepsOn(state, depIds), depIds);
	}

	private IntegerOptionBuilder extrasInt(ConfigBuilder b, String key, String name, String tooltip,
		Consumer<Integer> setter, Supplier<Integer> getter, int def, int min, int max, int step, String... deps) {
		String[] all = new String[deps.length + 1];
		all[0] = "module.extras";
		System.arraycopy(deps, 0, all, 1, deps.length);
		return gatedInt(b, key, name, tooltip, setter, getter, def, min, max, step, all);
	}

	private static boolean allDepsOn(net.caffeinemc.mods.sodium.api.config.ConfigState state, Identifier... deps) {
		for (Identifier dep : deps) {
			if (!state.readBooleanOption(dep)) {
				return false;
			}
		}
		return true;
	}

	private static Identifier[] ids(String... keys) {
		Identifier[] out = new Identifier[keys.length];
		for (int i = 0; i < keys.length; i++) {
			out[i] = id(keys[i]);
		}
		return out;
	}

	private static PerfConfig.ExtrasConfig extras() {
		return PerfConfig.get().extras;
	}

	private static int roundPow2(int value, int min, int max) {
		int v = Math.max(min, Math.min(max, value));
		int lower = Integer.highestOneBit(v);
		int upper = lower << 1;
		if (v - lower >= upper - v && upper <= max) {
			return upper;
		}
		return Math.max(min, lower);
	}

	@Override
	public void afterSave() {
		PerfConfig.save();
	}
}
