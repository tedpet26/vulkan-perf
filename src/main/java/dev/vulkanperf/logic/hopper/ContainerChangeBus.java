package dev.vulkanperf.logic.hopper;

import java.util.Iterator;
import java.util.Map;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

/**
 * Wake-up bus for sleeping hoppers (and other change-sensitive block entities).
 *
 * <p>A hopper may only sleep when every input that could change its behaviour is watched:
 * its own inventory, the container it extracts from, the container it inserts into, and
 * the item-entity zone above it. This registry tracks which positions are watched per level
 * so the hot paths stay O(1): {@code BlockEntity#setChanged} does one map lookup, and entity
 * movement only touches the sections the entity's bounding box overlaps.
 */
public final class ContainerChangeBus {
	private static final Map<Level, PerLevel> LEVELS = new java.util.WeakHashMap<>();

	private static volatile boolean hasWatchers;

	/** Hot-path check for entity movement: zero cost when no sleeping hoppers exist. */
	public static boolean hasWatchers() {
		return hasWatchers;
	}

	public static void clearLevel(Level level) {
		LEVELS.remove(level);
		hasWatchers = !LEVELS.isEmpty();
	}

	/**
	 * Called from {@code BlockEntity#setChanged}: wakes every sleeping block entity registered
	 * for this position. Cheap enough to run for every changed block entity in the world.
	 */
	public static void onBlockEntityChanged(BlockEntity be) {
		Level level = be.getLevel();
		if (level instanceof ServerLevel serverLevel) {
			PerLevel perLevel = LEVELS.get(level);
			if (perLevel != null) {
				perLevel.wakeAt(serverLevel, be.getBlockPos());
			}
		}
	}

	/**
	 * Called when a block update touches a position (neighbour change, place, remove):
	 * wakes sleepers that registered that position.
	 */
	public static void onBlockUpdate(Level level, BlockPos pos) {
		if (level instanceof ServerLevel serverLevel) {
			PerLevel perLevel = LEVELS.get(level);
			if (perLevel != null) {
				perLevel.wakeAt(serverLevel, pos);
			}
		}
	}

	/** Wakes sleepers whose watched zone (the block above them) intersects the entity's bounding box. */
	public static void onEntityBoxMoved(ServerLevel level, Entity entity) {
		PerLevel perLevel = LEVELS.get(level);
		if (perLevel == null || perLevel.watchersBySection.isEmpty()) {
			return;
		}		AABB box = entity.getBoundingBox();
		int xMin = SectionPos.posToSectionCoord(box.minX - 1.0);
		int yMin = SectionPos.posToSectionCoord(box.minY - 1.0);
		int zMin = SectionPos.posToSectionCoord(box.minZ - 1.0);
		int xMax = SectionPos.posToSectionCoord(box.maxX + 1.0);
		int yMax = SectionPos.posToSectionCoord(box.maxY + 1.0);
		int zMax = SectionPos.posToSectionCoord(box.maxZ + 1.0);
		for (int x = xMin; x <= xMax; x++) {
			for (int y = yMin; y <= yMax; y++) {
				for (int z = zMin; z <= zMax; z++) {
					LongSet sleepers = perLevel.watchersBySection.get(SectionPos.asLong(x, y, z));
					if (sleepers != null && !sleepers.isEmpty()) {
						Iterator<Long> it = sleepers.iterator();
						while (it.hasNext()) {
							BlockPos sleeperPos = BlockPos.of(it.next());
							BlockPos zone = sleeperPos.above();
							AABB zoneBox = new AABB(zone.getX(), zone.getY(), zone.getZ(), zone.getX() + 1.0, zone.getY() + 2.0, zone.getZ() + 1.0);
							if (zoneBox.intersects(box)) {
								it.remove();
								BlockEntity be = level.getBlockEntity(sleeperPos);
								if (be instanceof dev.vulkanperf.logic.sleeping.SleepingBlockEntity sleeper) {
									sleeper.vp$wakeUpNow();
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Registers a watcher: the sleeper at {@code sleeperPos} wants wake-ups for changes at
	 * {@code watchedPos}; it is also tracked for entity intersections in the section of the
	 * zone above itself.
	 */
	public static void watch(ServerLevel level, BlockPos sleeperPos, BlockPos watchedPos) {
		PerLevel perLevel = LEVELS.computeIfAbsent(level, l -> new PerLevel());
		hasWatchers = true;
		perLevel.watchersByPosition.computeIfAbsent(watchedPos.asLong(), k -> new LongOpenHashSet()).add(sleeperPos.asLong());
		SectionPos zoneSection = SectionPos.of(sleeperPos.above());
		perLevel.watchersBySection.computeIfAbsent(zoneSection.asLong(), k -> new LongOpenHashSet()).add(sleeperPos.asLong());
	}

	public static void unwatchAll(ServerLevel level, BlockPos sleeperPos) {
		PerLevel perLevel = LEVELS.get(level);
		if (perLevel == null) {
			return;
		}
		long sleeper = sleeperPos.asLong();
		for (Iterator<LongSet> it = perLevel.watchersByPosition.values().iterator(); it.hasNext(); ) {
			LongSet set = it.next();
			set.rem(sleeper);
			if (set.isEmpty()) {
				it.remove();
			}
		}
		for (Iterator<LongSet> it = perLevel.watchersBySection.values().iterator(); it.hasNext(); ) {
			LongSet set = it.next();
			set.rem(sleeper);
			if (set.isEmpty()) {
				it.remove();
			}
		}
	}

	private static final class PerLevel {
		/** watched pos -> sleeper positions listening to it. */
		final Long2ObjectMap<LongSet> watchersByPosition = new Long2ObjectOpenHashMap<>();
		/** section of the item zone above a sleeper -> sleeper positions. */
		final Long2ObjectMap<LongSet> watchersBySection = new Long2ObjectOpenHashMap<>();

		void wakeAt(ServerLevel level, BlockPos pos) {
			LongSet sleepers = this.watchersByPosition.remove(pos.asLong());
			if (sleepers != null && !sleepers.isEmpty()) {
				for (long sleeperEntry : sleepers) {
					BlockPos sleeperPos = BlockPos.of(sleeperEntry);
					BlockEntity be = level.getBlockEntity(sleeperPos);
					if (be instanceof dev.vulkanperf.logic.sleeping.SleepingBlockEntity sleeper) {
						sleeper.vp$wakeUpNow();
					}
				}
			}
		}
	}
}
