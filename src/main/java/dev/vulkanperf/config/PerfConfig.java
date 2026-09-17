package dev.vulkanperf.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PerfConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger("vulkanperf");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final int CONFIG_VERSION = 6;
	private static PerfConfig instance = new PerfConfig();

	public int configVersion = 0;

	public LogicConfig logic = new LogicConfig();
	public ChunksConfig chunks = new ChunksConfig();
	public PacketsConfig packets = new PacketsConfig();
	public MemoryConfig memory = new MemoryConfig();
	public LoggingConfig logging = new LoggingConfig();
	public PowerConfig power = new PowerConfig();
	public ParticlesConfig particles = new ParticlesConfig();
	public CullingConfig culling = new CullingConfig();
	public ClientCacheConfig clientcache = new ClientCacheConfig();
	public HudSpreadConfig hudspread = new HudSpreadConfig();
	public ReloadUiConfig reloadui = new ReloadUiConfig();
	public PingConfig ping = new PingConfig();
	public ExtrasConfig extras = new ExtrasConfig();
	public ImFastConfig imfast = new ImFastConfig();
	public NetworkConfig network = new NetworkConfig();
	public MoreCullingConfig moreculling = new MoreCullingConfig();
	public MfixConfig mfix = new MfixConfig();

	public static PerfConfig get() {
		return instance;
	}

	public static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("vulkanperf.json");
	}

	public static void load() {
		Path file = path();
		if (Files.isRegularFile(file)) {
			try (Reader reader = Files.newBufferedReader(file)) {
				PerfConfig loaded = GSON.fromJson(reader, PerfConfig.class);
				if (loaded != null) {
					instance = loaded;
					instance.fillNulls();
					instance.migrate();
					instance.sanitize();
				}
			} catch (IOException e) {
				LOGGER.warn("Failed to read {}", file, e);
			}
		} else {
			instance = new PerfConfig();
			instance.fillNulls();
			instance.configVersion = CONFIG_VERSION;
			instance.sanitize();
		}
		save();
	}

	public static void save() {
		Path file = path();
		try {
			Files.createDirectories(file.getParent());
			try (Writer writer = Files.newBufferedWriter(file)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException e) {
			LOGGER.warn("Failed to write {}", file, e);
		}
	}

	private void migrate() {
		if (this.configVersion < 4) {
			// Performance preset: enable Lithium-class subflags that were off by default.
			this.logic.collisionCache = true;
			this.logic.hopper = true;
			this.logic.inactiveAi = true;
			this.logic.voxelShapes = true;
			this.logic.pathCache = true;
			this.logic.itemMerge = true;
			this.logic.mobAiSkip = true;
			this.logic.hopperSleep = true;
			this.logic.joinIsNotEmptyCache = true;
			this.logic.pathTypeCache = true;
			this.memory.enabled = true;
			this.culling.entities = true;
		}
		if (this.configVersion < 5) {
			// v5: the packets module is on by default (its mixins redirect vanilla
			// constants to these same values, so nothing changes until a limit is
			// raised) — required so the PacketFixer break always ships a working
			// replacement.
			this.packets.enabled = true;
		}
		if (this.configVersion < 6) {
			// v6: 26.3 raised the vanilla decompressed cap in CompressionDecoder
			// from 2 MiB to 8 MiB; configs saved with the old 2 MiB default would
			// silently clamp harder than vanilla.
			if (this.packets.compression == 2_097_152) {
				this.packets.compression = 8_388_608;
			}
		}
		this.configVersion = CONFIG_VERSION;
	}

	private void sanitize() {
		if (this.memory.fastMapPropertyMap && !this.memory.fastMapNeighborLookup) {
			LOGGER.warn("memory.fastMapPropertyMap requires memory.fastMapNeighborLookup; disabling property-map replacement");
			this.memory.fastMapPropertyMap = false;
		}
	}

	private void fillNulls() {
		if (logic == null) logic = new LogicConfig();
		if (chunks == null) chunks = new ChunksConfig();
		if (packets == null) packets = new PacketsConfig();
		if (memory == null) memory = new MemoryConfig();
		if (logging == null) logging = new LoggingConfig();
		if (power == null) power = new PowerConfig();
		if (particles == null) particles = new ParticlesConfig();
		if (culling == null) culling = new CullingConfig();
		if (clientcache == null) clientcache = new ClientCacheConfig();
		if (hudspread == null) hudspread = new HudSpreadConfig();
		if (reloadui == null) reloadui = new ReloadUiConfig();
		if (ping == null) ping = new PingConfig();
		if (extras == null) extras = new ExtrasConfig();
		if (imfast == null) imfast = new ImFastConfig();
		if (network == null) network = new NetworkConfig();
		if (moreculling == null) moreculling = new MoreCullingConfig();
		if (mfix == null) mfix = new MfixConfig();
	}

	public static final class LogicConfig {
		public boolean enabled = true;
		public boolean collisionCache = true;
		public boolean hopper = true;
		public boolean inactiveAi = true;
		public boolean voxelShapes = true;
		public boolean pathCache = true;
		public boolean itemMerge = true;
		public boolean mobAiSkip = true;

		// Tier S expansion
		/** Extend the hopper idle cooldown and reset it when the hopper's inventory changes. */
		public boolean hopperSleep = true;
		/** Cache Shapes#joinIsNotEmpty results for identical shape/op triples (occlusion, collision checks). */
		public boolean joinIsNotEmptyCache = true;
		/** Cache WalkNodeEvaluator#getPathTypeFromState results per BlockState instance, shared across mobs. */
		public boolean pathTypeCache = true;
		/** Skip random-tick iteration for all-air sections. */
		public boolean randomTickSkip = true;
		/** Cache FluidState#isRandomlyTicking per fluid-state instance (constant per state, hot in random-tick loops). */
		public boolean fluidRandomTickCache = true;

		// Full-parity expansion (Tier S entity/collision)
		/** Class-group filtered hard-collision queries + per-section collision indexes. */
		public boolean entityCollisionGroups = true;
		/** Lazy collider materialisation for Entity#collide movement sweeps. */
		public boolean entityFastMovement = true;
		/** Direct section lookups for small-box entity queries. */
		public boolean entityFastRetrieval = true;
		/** Sleeping block entities: idle block entities park their ticker until something changes. */
		public boolean sleepingBlockEntities = true;
		/** Cache Shapes#joinIsNotEmpty cuboid fast path (VoxelShapeMatchesAnywhere equivalent). */
		public boolean shapesCuboidMatch = true;
		/** Cache World#noCollision entity half with class groups (no shape building for empty checks). */
		public boolean fastNoCollision = true;
		/** Keep inactive mob navigations out of the block-update notification set. */
		public boolean inactiveNavigations = true;
		/** int-keyed LevelChunkTicks rewrite (scheduled block/fluid ticks). */
		public boolean tickScheduler = true;
		/** Raycast without lambda allocation per clip call. */
		public boolean fastRaycast = true;
		/** Explosion position/air-count caches. */
		public boolean explosionOpts = true;
		/** Math overwrites: BlockPos/Direction/AABB helpers and sine LUT. */
		public boolean mathOpts = true;
		/** Allocation trims: enum values, composter, entity iteration, NBT copy. */
		public boolean allocOpts = true;
		/** POI lookup overwrites (no Streams in find/getInRange paths). */
		public boolean poiOpts = true;
		/** Per-blockstate neighbor path-type cache + node evaluator short-circuits. */
		public boolean pathNeighborCache = true;
		/** Combined single-pass heightmap update on chunk setBlockState. */
		public boolean combinedHeightmap = true;
		/** Redstone wire redundant update suppression. */
		public boolean redstoneOpts = true;
		/** Fluid spread caching. */
		public boolean fluidFlowOpts = true;
		/** Game event dispatch distance skips. */
		public boolean gameEventOpts = true;
		/** Cached noise generator settings accessor. */
		public boolean cachedGenSettings = true;
	}

	public static final class ChunksConfig {
		public boolean enabled = true;
		public int worldgenThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
		public int serializeThreads = 2;
		public int ioThreads = 2;

		// 1. Scheduling: mid-tick chunk task draining.
		public boolean midTickScheduling = true;
		public long midTickIntervalNanos = 2_000_000L;

		// 2. Async IO: region file cache limits + executor rewiring.
		public boolean asyncIoDeepened = true;
		public int regionFileCacheSize = 256;

		// C2ME-class worldgen micro-optimizations: alloc trims, random swaps, structure-sync fixes.
		public boolean worldgenOpts = true;

		// natives / OpenCL acceleration: unimplemented in this slice, always OFF.
		public boolean nativesMath = false;
		public boolean openclAccel = false;

		// Full chunk-system rewrite (ticket/holder replacement): deferred.
		public boolean rewriteChunkSystem = false;

		// 3. Client-side: raise the render-distance slider cap beyond vanilla's 32.
		public boolean clientViewDistanceUncap = true;
		public int clientMaxViewDistance = 48;
	}

	public static final class PacketsConfig {
		/** On by default: the three mixins redirect vanilla constants to these same
		 * values, so a fresh install changes nothing until a limit is raised.
		 * Needed on-by-default because fabric.mod.json breaks PacketFixer. */
		public boolean enabled = true;
		public int nbtQuota = 2_097_152;
		public int stringSize = 32767;
		public int compression = 8_388_608;
	}

	public static final class MemoryConfig {
		public boolean enabled = true;
		/** Reuse identical {@code Shapes#join} results (existing join intern). */
		public boolean internShapes = true;
		/** Shared neighbor table per block instead of a per-state {@code S[][]} (Ferrite NEIGHBOR_LOOKUP). */
		public boolean fastMapNeighborLookup = true;
		/** Drop the per-state {@code propertyValues} array and read values from the neighbor table (needs neighbor lookup). */
		public boolean fastMapPropertyMap = true;
		/** Intern structurally equal collision shapes / face-sturdy tables on {@code BlockStateBase.Cache}. */
		public boolean blockStateCacheDedup = true;
		/** Share one immutable empty patch map across item stacks that have no component overrides. */
		public boolean dataComponentPatchSharing = true;
		/** Opt-in: denser FastMap index (smaller table, integer division). Default off. */
		public boolean compactFastMap = false;
		/** Opt-in: replace {@code PalettedContainer}'s {@code ThreadingDetector} with a single byte. Default off. */
		public boolean smallThreadDetector = false;
	}

	public static final class LoggingConfig {
		public boolean enabled = true;
	}

	public static final class PowerConfig {
		public boolean enabled = true;
		public int unfocusedFps = 10;
		public int hiddenFps = 1;
		public boolean muteUnfocused = true;
	}

	public static final class ParticlesConfig {
		public boolean enabled = true;
		public boolean frustumCull = true;
		/** Cull particles by bounding box against the frustum (better for large particles). */
		public boolean frustumBoundingBox = true;
		/** Skip particles beyond an effective range derived from the render distance. */
		public boolean renderDistanceCull = true;
		/** Multiplier applied to the render distance for the particle cull range. */
		public double renderDistanceMultiplier = 1.25;
		/** Cache particle lightmap lookups once per game tick. */
		public boolean lightCache = true;
	}

	public static final class CullingConfig {
		public boolean enabled = true;
		public boolean entities = true;
		public boolean blockEntities = true;
		public int entityCacheTicks = 10;
		public double entityMaxDistance = 128.0;
	}

	public static final class ClientCacheConfig {
		public boolean enabled = true;
		public boolean toasts = true;
		public boolean sky = true;
	}

	public static final class HudSpreadConfig {
		public boolean enabled = true;
		public int spreadFrames = 2;
	}

	public static final class ReloadUiConfig {
		public boolean enabled = true;
		public boolean skipAfterFirst = true;
	}

	public static final class PingConfig {
		public boolean enabled = true;
	}

	public static final class ExtrasConfig {
		public boolean enabled = true;
		public boolean animation = true;
		public boolean animateWater = true;
		public boolean animateLava = true;
		public boolean animateFire = true;
		public boolean animatePortal = true;
		public boolean animateBlocks = true;
		public boolean animateSculk = true;
		public boolean particles = true;
		public boolean rainSplash = true;
		public boolean blockBreak = true;
		public boolean blockBreaking = true;
		public boolean weatherParticles = true;
		public boolean sky = true;
		public boolean sun = true;
		public boolean moon = true;
		public boolean stars = true;
		public boolean rainSnow = true;
		public boolean biomeColors = true;
		public boolean skyColors = true;
		public boolean fog = true;
		public boolean lightUpdates = true;
		public boolean itemFrames = true;
		public boolean armorStands = true;
		public boolean paintings = true;
		public boolean pistons = true;
		public boolean beaconBeams = true;
		public boolean enchantingTableBook = true;
		public boolean nametags = true;
		public boolean itemFrameNametags = true;
		public boolean toasts = true;
		public boolean overlayFps = true;
		public boolean overlayFpsExtended = false;
		public boolean overlayCoords = false;
		public int overlayUpdateMs = 500;
	}

	/**
	 * ImmediatelyFast-class client rendering optimizations (batching, atlases, GL-only tricks).
	 */
	public static final class ImFastConfig {
		public boolean enabled = true;

		/** Force {@code RenderTypeFeatureRenderer$Group} to always allow draw-call reordering/merging. */
		public boolean enhancedBatching = true;
		/** Pack map textures into shared GPU atlases instead of one texture per map id. */
		public boolean mapAtlasGeneration = true;
		/** Edge length of each packed map atlas sheet (power of two). */
		public int mapAtlasSize = 2048;
		/** Grow the glyph atlas texture beyond the vanilla 256x256 to reduce texture switches. */
		public boolean fontAtlasResizing = true;
		public int fontAtlasSize = 1024;
		/** Cache the last resolved VertexConsumer per glyph renderer to skip redundant lookups. */
		public boolean fastTextLookup = true;
		/** Disable vertex-sorting for polygon-offset/see-through text render types (opaque quads, sorting is wasted work). */
		public boolean skipTextTranslucencySorting = true;
		/** Route frequently-changing (animated) item icons to a dedicated small atlas so they don't invalidate the main GUI item atlas every frame. */
		public boolean batchAnimatedItemUpdates = true;
		/** GL backend only: skip the redundant framebuffer unbind between render passes / before swap. */
		public boolean avoidRedundantFramebufferSwitching = true;
		/** GL backend + Apple GPU only: use bufferData instead of bufferSubData for full-buffer uploads. */
		public boolean fixSlowBufferUploadOnAppleGpu = true;
		/** Rasterize static sign text into a shared atlas (experimental; skipped when Iris is loaded). */
		public boolean signTextBuffering = false;
		/** Edge length of the experimental sign-text atlas (power of two). */
		public int signAtlasSize = 4096;
		/** Temporarily disable font/map atlas opts when a resource pack replaces core text shaders. */
		public boolean resourcePackConflictHandling = true;
		/** GL backend only: print a stack trace with OpenGL debug callback messages (noisy). */
		public boolean printAdditionalErrorInformation = false;
		/** Per-element-list bounds union so GuiRenderState#hasIntersection short-circuits instead of scanning every element. */
		public boolean guiIntersectionFastPath = true;
	}

	/**
	 * Krypton-class network micro-optimizations (frame decode, varint/string encode, shared
	 * prepender). Native compression/encryption stay upstream-only (see README).
	 */
	public static final class NetworkConfig {
		public boolean enabled = true;
	}
	/**
	 * MoreCulling-class block/entity render culling. The leaves face culling core is already
	 * vanilla in 26.3 (cutout leaves); the remaining wins live here.
	 */
	public static final class MoreCullingConfig {
		public boolean enabled = true;
		/** Skip sign text on the side of the sign facing away from the camera. */
		public boolean signTextBackFace = true;
	}

	/**
	 * ModernFix-class startup/memory fixes. Lazy search trees and the encoder-cache soft
	 * references are already vanilla in 26.3; the remaining wins live here.
	 */
	public static final class MfixConfig {
		public boolean enabled = true;
		/** Index zip resource packs once instead of re-walking the central directory per query. */
		public boolean zipIndex = true;
		/** Expire decoded OpenAL sound buffers after 30s of disuse. */
		public boolean dynamicSounds = true;
		/** Soft-reference the structure template cache so cold templates can be GC'd. */
		public boolean dynamicStructures = true;
	}
}