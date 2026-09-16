package dev.vulkanperf.memory.fastmap;

import net.minecraft.world.level.block.state.properties.Property;

import java.util.Arrays;

/**
 * Replaces the O(n^2)-ish per-state "neighbor" arrays vanilla builds for every
 * blockstate permutation with a single flat value matrix shared by every
 * state of a block. Looking up a "neighbor" state (the result of changing one
 * property's value) becomes an index transform plus one array read, instead
 * of holding a dedicated array of references per property per state.
 */
public final class StateTable<S> {
	private final Property<?>[] properties;
	private final PropertyIndexer[] indexers;
	private final Object[] slots;

	public StateTable(Property<?>[] properties, boolean compact) {
		this.properties = properties;
		this.indexers = new PropertyIndexer[properties.length];
		int span = 1;
		for (int i = 0; i < properties.length; i++) {
			var possible = properties[i].getPossibleValues();
			int valueCount = possible.size();
			for (int valueIndex = 0; valueIndex < valueCount; valueIndex++) {
				@SuppressWarnings({"unchecked", "rawtypes"})
				int internal = ((Property) properties[i]).getInternalIndex((Comparable) possible.get(valueIndex));
				if (internal != valueIndex) {
					throw new IllegalStateException(
							"Property " + properties[i] + " value index mismatch: possibleValues[" + valueIndex + "] maps to " + internal
					);
				}
			}
			PropertyIndexer indexer = compact ? new DensePackedIndexer(span, valueCount) : BitPackedIndexer.of(span, valueCount);
			this.indexers[i] = indexer;
			span *= indexer.span();
		}
		this.slots = new Object[span];
	}

	/**
	 * Places {@code state} into the table using the values reported by
	 * {@code lookup}, returning the flat index it was assigned.
	 */
	public int place(S state, PropertyValueLookup<S> lookup) {
		int index = 0;
		for (int i = 0; i < properties.length; i++) {
			@SuppressWarnings({"unchecked", "rawtypes"})
			int valueIndex = ((Property) properties[i]).getInternalIndex((Comparable) lookup.valueOf(state, properties[i]));
			index += indexers[i].baseOffset(valueIndex);
		}
		if (slots[index] != null) {
			throw new IllegalStateException("Duplicate state table slot " + index + " for " + state);
		}
		slots[index] = state;
		return index;
	}

	/**
	 * @return the state obtained by changing property {@code propertyIndex} of
	 * the state at {@code index} to {@code valueIndex}
	 */
	@SuppressWarnings("unchecked")
	public S neighbor(int index, int propertyIndex, int valueIndex) {
		int target = indexers[propertyIndex].moveTo(index, valueIndex);
		Object result = target < 0 ? null : slots[target];
		if (result == null) {
			throw new IllegalArgumentException(
					"No state for property " + propertyIndex + " = " + valueIndex + " starting from " + index +
							" (properties: " + Arrays.toString(properties) + ")"
			);
		}
		return (S) result;
	}

	public int valueIndexAt(int index, int propertyIndex) {
		return indexers[propertyIndex].extract(index);
	}

	public Property<?>[] properties() {
		return properties;
	}

	@FunctionalInterface
	public interface PropertyValueLookup<S> {
		Comparable<?> valueOf(S state, Property<?> property);
	}
}
