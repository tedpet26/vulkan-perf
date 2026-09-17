package dev.vulkanperf.logic.entity;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.ArrayList;

import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A list of collision shapes whose elements are pulled from a backing iterator lazily and cached.
 *
 * <p>Vanilla {@code Entity#collide} materialises every block collision shape of the swept bounding
 * box into an immutable list up front, even when the movement dies on the first shape (the
 * overwhelmingly common case for entities standing on ground). Feeding {@code Shapes.collide} a
 * lazily-filled list instead means only the shapes actually consulted by the per-axis sweep are
 * ever created; repeated traversal (once per movement axis, plus step-up candidates) reads the
 * cached prefix.
 */
public final class LazyColliderList extends AbstractList<VoxelShape> {
	private final ArrayList<VoxelShape> filled = new ArrayList<>();
	private final Iterator<VoxelShape> source;
	private boolean exhausted;

	public LazyColliderList(Iterator<VoxelShape> source) {
		this.source = source;
	}

	public static LazyColliderList of(Iterable<VoxelShape> iterable) {
		return new LazyColliderList(iterable.iterator());
	}

	private void fillUpTo(int index) {
		while (!this.exhausted && this.filled.size() <= index) {
			if (!this.source.hasNext()) {
				this.exhausted = true;
				return;
			}
			this.filled.add(this.source.next());
		}
	}

	@Override
	public VoxelShape get(int index) {
		this.fillUpTo(index);
		return this.filled.get(index);
	}

	@Override
	public int size() {
		if (this.exhausted) {
			return this.filled.size();
		}
		this.fillUpTo(Integer.MAX_VALUE >> 1);
		return this.filled.size();
	}

	@Override
	public Iterator<VoxelShape> iterator() {
		return new LazyIterator();
	}

	private final class LazyIterator implements Iterator<VoxelShape> {
		private int cursor;

		@Override
		public boolean hasNext() {
			if (this.cursor < LazyColliderList.this.filled.size()) {
				return true;
			}
			if (LazyColliderList.this.exhausted) {
				return false;
			}
			if (LazyColliderList.this.source.hasNext()) {
				LazyColliderList.this.filled.add(LazyColliderList.this.source.next());
				return true;
			}
			LazyColliderList.this.exhausted = true;
			return false;
		}

		@Override
		public VoxelShape next() {
			this.hasNext();
			return LazyColliderList.this.filled.get(this.cursor++);
		}
	}
}
