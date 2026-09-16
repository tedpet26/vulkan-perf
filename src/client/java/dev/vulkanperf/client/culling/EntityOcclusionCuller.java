package dev.vulkanperf.client.culling;

import dev.vulkanperf.config.PerfConfig;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Entity occlusion culling, Entity Culling-style:
 * <ul>
 *   <li>out-of-frustum entities are rejected by the caller mixin before any
 *       occlusion work is scheduled (vanilla culls those anyway),</li>
 *   <li>occlusion probes a small set of AABB points against the camera, one
 *       raycast per check,</li>
 *   <li>raycasts run on a small worker pool; the render thread uses each
 *       entity's last known result while a test is in flight, defaulting to
 *       visible for never-tested entities (fail-open).</li>
 * </ul>
 * The result cache is tick-based (entityCacheTicks); rechecks are time-based.
 */
public final class EntityOcclusionCuller {
	/** How often (ms) an entity with a cached INVISIBLE result gets rechecked. */
	private static final long INVISIBLE_RECHECK_MS = 500;
	/** How often (ms) an entity with a cached VISIBLE result gets rechecked. */
	private static final long VISIBLE_RECHECK_MS = 2000;
	/** Distance from the camera below which entities are always rendered. */
	private static final double NEAR_SQ = 4.0;
	/** The AABB probe points (center + 6 face centers), relative to the box. */
	private static final double[][] PROBES = {
		{0.5, 0.5, 0.5},
		{0.5, 0.0, 0.5},
		{0.5, 1.0, 0.5},
		{0.0, 0.5, 0.5},
		{1.0, 0.5, 0.5},
		{0.5, 0.5, 0.0},
		{0.5, 0.5, 1.0},
	};

	private enum State {
		VISIBLE, INVISIBLE, PENDING
	}

	private static final Int2ObjectOpenHashMap<Entry> CACHE = new Int2ObjectOpenHashMap<>();
	private static long lastPruneTick = Long.MIN_VALUE;

	/** Dedicated single-thread executor; occlusion raycasts are cheap but frequent. */
	private static final class Holder {
		private Holder() {
		}

		static final java.util.concurrent.ExecutorService EXECUTOR =
			java.util.concurrent.Executors.newSingleThreadExecutor(runnable -> {
				Thread thread = new Thread(runnable, "vulkanperf-occlusion");
				thread.setDaemon(true);
				return thread;
			});
	}

	private EntityOcclusionCuller() {
	}

	/**
	 * Render-thread entry: true = render, false = cull. Never blocks; while a
	 * test is in flight the last known result applies (visible by default).
	 */
	public static boolean isVisible(Entity entity, Camera camera) {
		PerfConfig.CullingConfig cfg = PerfConfig.get().culling;
		if (!cfg.enabled || !cfg.entities || entity == null || camera == null) {
			return true;
		}
		if (entity instanceof Player || entity.isSpectator() || entity.isInvisible()) {
			return true;
		}
		Minecraft client = Minecraft.getInstance();
		Level level = client.level;
		if (level == null) {
			return true;
		}

		AABB box = entity.getBoundingBox();
		Vec3 cam = camera.position();
		double cx = (box.minX + box.maxX) * 0.5;
		double cy = (box.minY + box.maxY) * 0.5;
		double cz = (box.minZ + box.maxZ) * 0.5;
		double dx = cx - cam.x;
		double dy = cy - cam.y;
		double dz = cz - cam.z;
		double distSq = dx * dx + dy * dy + dz * dz;
		double max = cfg.entityMaxDistance;
		if (distSq > max * max) {
			return false;
		}
		if (distSq < NEAR_SQ) {
			return true;
		}

		long tick = level.getGameTime();
		prune(tick, cfg.entityCacheTicks);
		int id = entity.getId();
		Entry entry = CACHE.get(id);
		if (entry == null) {
			entry = new Entry();
			CACHE.put(id, entry);
		}
		entry.tick = tick;

		long now = System.currentTimeMillis();
		if (entry.state == State.PENDING) {
			// A test is in flight: keep last-known behavior.
			return entry.lastVisible;
		}
		long recheckAfter = entry.state == State.VISIBLE ? VISIBLE_RECHECK_MS : INVISIBLE_RECHECK_MS;
		if (now - entry.lastTestMs < recheckAfter) {
			return entry.state == State.VISIBLE;
		}

		schedule(level, entity, cam, box, entry, now);
		return entry.lastVisible;
	}

	private static void schedule(Level level, Entity entity, Vec3 cam, AABB box, Entry entry, long now) {
		entry.state = State.PENDING;
		entry.lastTestMs = now;
		final Vec3[] targets = probeTargets(box, cam);
		final Vec3 camRef = cam;
		Holder.EXECUTOR.execute(() -> {
			try {
				if (entity.isRemoved()) {
					entry.state = State.VISIBLE;
					return;
				}
				boolean visible = testPoints(level, camRef, targets);
				entry.lastVisible = visible;
				entry.state = visible ? State.VISIBLE : State.INVISIBLE;
			} catch (Throwable ignored) {
				entry.state = State.VISIBLE; // fail open on any worker error
			}
		});
	}

	/** Render-thread: compute the probe targets once per scheduled test. */
	private static Vec3[] probeTargets(AABB box, Vec3 cam) {
		Vec3[] targets = new Vec3[PROBES.length];
		for (int i = 0; i < PROBES.length; i++) {
			targets[i] = new Vec3(
				box.minX + (box.maxX - box.minX) * PROBES[i][0],
				box.minY + (box.maxY - box.minY) * PROBES[i][1],
				box.minZ + (box.maxZ - box.minZ) * PROBES[i][2]);
		}
		return targets;
	}

	/** Worker thread: raycast from the camera; visible if any probe is unobstructed. */
	private static boolean testPoints(Level level, Vec3 cam, Vec3[] targets) {
		for (Vec3 target : targets) {
			ClipContext context = new ClipContext(cam, target, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty());
			BlockHitResult hit = level.clip(context);
			if (hit.getType() == HitResult.Type.MISS) {
				return true;
			}
			double tdx = target.x - cam.x;
			double tdy = target.y - cam.y;
			double tdz = target.z - cam.z;
			double targetDistSq = tdx * tdx + tdy * tdy + tdz * tdz;
			if (hit.getLocation().distanceToSqr(cam) >= targetDistSq - 0.25) {
				return true;
			}
		}
		return false;
	}

	private static void prune(long tick, int retain) {
		if (tick == lastPruneTick) {
			return;
		}
		lastPruneTick = tick;
		if (CACHE.size() < 256) return;
		CACHE.values().removeIf(entry -> tick - entry.tick >= retain * 4L);
		if (CACHE.size() > 2048) {
			CACHE.clear();
		}
	}

	private static final class Entry {
		/** Written by the worker thread, read by the render thread (hence volatile). */
		private volatile State state = State.PENDING;
		/** Written by the worker thread, read by the render thread (hence volatile). */
		private volatile boolean lastVisible = true;
		private long tick;
		private long lastTestMs;
	}
}
