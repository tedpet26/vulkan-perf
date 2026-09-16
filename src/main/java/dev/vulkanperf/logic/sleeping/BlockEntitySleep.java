package dev.vulkanperf.logic.sleeping;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Lithium-style sleeping block entities: track "did anything change last tick"
 * per block-entity position so furnace/brewing-type tickers can skip when fully
 * idle and wake on setChanged / inventory interaction / neighbor change.
 */
public final class BlockEntitySleep {
	private static final ConcurrentHashMap<Long, long[]> LAST_ACTIVITY = new ConcurrentHashMap<>();

	private BlockEntitySleep() {
	}

	/** Record activity (setChanged, container open, recipe state change). */
	public static void wake(long pos) {
		LAST_ACTIVITY.remove(pos);
	}

	/**
	 * @return true when the ticker may skip this tick (no recent activity and
	 * static block state), false when it must tick.
	 */
	public static boolean canSkip(Level level, BlockPos pos, BlockState state, BlockEntity entity) {
		if (!PerfConfig.get().logic.sleepingBlockEntities) {
			return false;
		}
		long key = pos.asLong();
		long now = level.getGameTime();
		long[] last = LAST_ACTIVITY.get(key);
		if (last != null && now - last[0] < 4) {
			return false;
		}
		// Redstone power or changing neighbors force a tick so behavior stays vanilla.
		if (level.hasNeighborSignal(pos) || state.hasAnalogOutputSignal() || (last != null && last[1] != state.hashCode())) {
			LAST_ACTIVITY.put(key, new long[]{now, state.hashCode()});
			return false;
		}
		if (last == null) {
			LAST_ACTIVITY.put(key, new long[]{now, state.hashCode()});
			return false;
		}
		return true;
	}

	public static void onRemove(long pos) {
		LAST_ACTIVITY.remove(pos);
	}

	public static void clear() {
		LAST_ACTIVITY.clear();
	}
}
