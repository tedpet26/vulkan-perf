package dev.vulkanperf.logic;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Reuses nearby-entity lists within the same game tick for the same AABB and
 * the same predicate instance. Different predicates never share a cached list.
 */
public final class EntitySectionIndex {
	private static final int SLOTS = 8;
	private static final ThreadLocal<ArrayList<Entity>> FILTER_BUFFER = ThreadLocal.withInitial(ArrayList::new);

	private final Slot[] slots = new Slot[SLOTS];
	private int next;

	public EntitySectionIndex() {
		for (int i = 0; i < SLOTS; i++) {
			this.slots[i] = new Slot();
		}
	}

	public List<Entity> hit(long tick, Entity except, AABB box, Predicate<? super Entity> predicate) {
		for (Slot slot : this.slots) {
			if (slot.tick == tick && slot.except == except && slot.predicate == predicate && slot.box != null && slot.box.equals(box)) {
				return slot.result;
			}
		}
		return null;
	}

	public void store(long tick, Entity except, AABB box, Predicate<? super Entity> predicate, List<Entity> result) {
		Slot slot = this.slots[this.next];
		this.next = (this.next + 1) % SLOTS;
		slot.tick = tick;
		slot.except = except;
		slot.predicate = predicate;
		slot.box = box;
		slot.result = result.isEmpty() ? List.of() : List.copyOf(result);
	}

	public static List<Entity> filter(List<Entity> source, AABB box, Predicate<? super Entity> predicate) {
		if (source.isEmpty()) {
			return List.of();
		}
		ArrayList<Entity> out = FILTER_BUFFER.get();
		out.clear();
		for (Entity entity : source) {
			if (entity != null && entity.getBoundingBox().intersects(box) && (predicate == null || predicate.test(entity))) {
				out.add(entity);
			}
		}
		return out.isEmpty() ? List.of() : List.copyOf(out);
	}

	private static final class Slot {
		private AABB box;
		private Entity except;
		private Predicate<? super Entity> predicate;
		private List<Entity> result = List.of();
		private long tick = Long.MIN_VALUE;
	}
}
