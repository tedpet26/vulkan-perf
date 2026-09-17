package dev.vulkanperf.logic.sleeping;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;

/**
 * The two ticker flavours a sleeping block entity parks inside its wrapper.
 *
 * <p>The idle ticker is a no-op: it reports no position so the chunk tick guard short-circuits it
 * (see {@code LevelTickBlocksGuardMixin}) while still reporting the entity's removed-state so the
 * ticker list stays accurate. The timed ticker wakes its block entity exactly once, used where
 * vanilla relies on a single follow-up tick (e.g. hopper cooldown parity).
 */
public final class SleepingTickers {
	public static final String SLEEPING_TYPE = "<vulkanperf_sleeping>";

	private SleepingTickers() {
	}

	public static TickingBlockEntity idle(BlockEntity owner, TickingBlockEntity real) {
		return new IdleTicker(owner, real);
	}

	public static TickingBlockEntity wakeNextTick(SleepingBlockEntity owner, BlockEntity entity, TickingBlockEntity real) {
		return new TimedWakeTicker(owner, entity, real);
	}

	private record IdleTicker(BlockEntity owner, TickingBlockEntity real) implements TickingBlockEntity {
		@Override
		public void tick() {
		}

		@Override
		public boolean isRemoved() {
			return this.real.isRemoved();
		}

		@Override
		public BlockPos getPos() {
			return null;
		}

		@Override
		public String getType() {
			return SLEEPING_TYPE;
		}
	}

	private record TimedWakeTicker(SleepingBlockEntity owner, BlockEntity entity, TickingBlockEntity real) implements TickingBlockEntity {
		@Override
		public void tick() {
			this.owner.vp$wakeUpNow();
			this.real.tick();
		}

		@Override
		public boolean isRemoved() {
			return this.real.isRemoved();
		}

		@Override
		public BlockPos getPos() {
			return this.real.getPos();
		}

		@Override
		public String getType() {
			return SLEEPING_TYPE;
		}
	}
}
