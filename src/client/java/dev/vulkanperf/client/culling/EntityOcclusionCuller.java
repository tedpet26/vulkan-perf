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
 * Entity occlusion via block clip from the camera to the entity. Results are
 * cached for a few ticks to avoid per-frame raycasts (Entity Culling–style).
 */
public final class EntityOcclusionCuller {
	private static final Int2ObjectOpenHashMap<Entry> CACHE = new Int2ObjectOpenHashMap<>();
	private static long lastPruneTick = Long.MIN_VALUE;

	private EntityOcclusionCuller() {
	}

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

		Vec3 cam = camera.position();
		AABB box = entity.getBoundingBox();
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
		if (distSq < 4.0) {
			return true;
		}

		long tick = level.getGameTime();
		prune(tick, cfg.entityCacheTicks);
		int id = entity.getId();
		Entry cached = CACHE.get(id);
		if (cached != null && tick - cached.tick < cfg.entityCacheTicks) {
			return cached.visible;
		}

		Vec3 target = new Vec3(cx, cy, cz);
		ClipContext context = new ClipContext(cam, target, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty());
		BlockHitResult hit = level.clip(context);
		boolean visible = hit.getType() == HitResult.Type.MISS
			|| hit.getLocation().distanceToSqr(cam) >= distSq - 0.25;

		CACHE.put(id, new Entry(tick, visible));
		return visible;
	}

	private static void prune(long tick, int retain) {
		if (tick == lastPruneTick) {
			return;
		}
		lastPruneTick = tick;
		if (CACHE.size() < 256) {
			return;
		}
		CACHE.values().removeIf(entry -> tick - entry.tick >= retain * 4L);
		if (CACHE.size() > 2048) {
			CACHE.clear();
		}
	}

	private record Entry(long tick, boolean visible) {
	}
}
