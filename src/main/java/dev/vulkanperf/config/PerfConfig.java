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
	private static final int CONFIG_VERSION = 3;
	private static PerfConfig instance = new PerfConfig();

	public int configVersion = 0;

	public LogicConfig logic = new LogicConfig();
	public ChunksConfig chunks = new ChunksConfig();
	public PacketsConfig packets = new PacketsConfig();
	public NetConfig net = new NetConfig();
	public MemoryConfig memory = new MemoryConfig();
	public LoggingConfig logging = new LoggingConfig();
	public PowerConfig power = new PowerConfig();
	public ParticlesConfig particles = new ParticlesConfig();
	public CullingConfig culling = new CullingConfig();
	public ClientCacheConfig clientcache = new ClientCacheConfig();
	public HudSpreadConfig hudspread = new HudSpreadConfig();
	public BatchingConfig batching = new BatchingConfig();
	public ReloadUiConfig reloadui = new ReloadUiConfig();
	public PingConfig ping = new PingConfig();
	public ExtrasConfig extras = new ExtrasConfig();
	public WindowConfig window = new WindowConfig();
	public InputConfig input = new InputConfig();
	public BlockEntitiesConfig blockentities = new BlockEntitiesConfig();
	public VanillaFixesConfig vanillafixes = new VanillaFixesConfig();

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
				}
			} catch (IOException e) {
				LOGGER.warn("Failed to read {}", file, e);
			}
		} else {
			instance = new PerfConfig();
			instance.fillNulls();
			instance.configVersion = CONFIG_VERSION;
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
		if (this.configVersion < 2) {
			this.logic.collisionCache = false;
		}
		if (this.configVersion < 3) {
			this.batching.enabled = false;
		}
		this.configVersion = CONFIG_VERSION;
	}

	private void fillNulls() {
		if (logic == null) logic = new LogicConfig();
		if (chunks == null) chunks = new ChunksConfig();
		if (packets == null) packets = new PacketsConfig();
		if (net == null) net = new NetConfig();
		if (memory == null) memory = new MemoryConfig();
		if (logging == null) logging = new LoggingConfig();
		if (power == null) power = new PowerConfig();
		if (particles == null) particles = new ParticlesConfig();
		if (culling == null) culling = new CullingConfig();
		if (clientcache == null) clientcache = new ClientCacheConfig();
		if (hudspread == null) hudspread = new HudSpreadConfig();
		if (batching == null) batching = new BatchingConfig();
		if (reloadui == null) reloadui = new ReloadUiConfig();
		if (ping == null) ping = new PingConfig();
		if (extras == null) extras = new ExtrasConfig();
		if (window == null) window = new WindowConfig();
		if (input == null) input = new InputConfig();
		if (blockentities == null) blockentities = new BlockEntitiesConfig();
		if (vanillafixes == null) vanillafixes = new VanillaFixesConfig();
	}

	public static final class LogicConfig {
		public boolean enabled = true;
		public boolean collisionCache = false;
		public boolean hopper = false;
		public boolean inactiveAi = false;
		public boolean voxelShapes = false;
		public boolean pathCache = false;
	}

	public static final class ChunksConfig {
		public boolean enabled = true;
		public int worldgenThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
		public int serializeThreads = 2;
		public int ioThreads = 2;
	}

	public static final class PacketsConfig {
		public boolean enabled = false;
		public int nbtQuota = 2_097_152;
		public int stringSize = 32767;
		public int compression = 2_097_152;
	}

	public static final class NetConfig {
		public boolean enabled = false;
		public boolean coalesceFlush = true;
	}

	public static final class MemoryConfig {
		public boolean enabled = false;
		public boolean internModels = true;
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
		public boolean asyncTick = false;
	}

	public static final class CullingConfig {
		public boolean enabled = true;
		public boolean entities = true;
		public boolean blockEntities = true;
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

	public static final class BatchingConfig {
		public boolean enabled = false;
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

	public static final class WindowConfig {
		public boolean enabled = false;
		public boolean borderlessFullscreen = false;
	}

	public static final class InputConfig {
		public boolean enabled = false;
	}

	public static final class BlockEntitiesConfig {
		public boolean enabled = false;
	}

	public static final class VanillaFixesConfig {
		public boolean enabled = false;
	}
}
