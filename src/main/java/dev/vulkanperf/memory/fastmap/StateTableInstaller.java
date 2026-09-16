package dev.vulkanperf.memory.fastmap;

import dev.vulkanperf.mixin.memory.accessors.StateHolderKeysAccessor;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Collection;

/**
 * Builds a shared {@link StateTable} for a freshly created group of sibling
 * states (all values of a block/property combination) and attaches it to
 * every state, replacing vanilla's per-state neighbor arrays.
 */
public final class StateTableInstaller {
	private StateTableInstaller() {
	}

	@SuppressWarnings("unchecked")
	public static <S extends StateHolder<?, S>> void install(Collection<S> states, boolean compact) {
		if (states.isEmpty()) {
			return;
		}
		S sample = states.iterator().next();
		Property<?>[] keys = ((StateHolderKeysAccessor) sample).vulkanperf$getPropertyKeys();
		StateTable<S> table = new StateTable<>(keys, compact);
		for (S state : states) {
			int index = table.place(state, StateHolder::getValue);
			((StateTableHolder<S>) state).vulkanperf$installStateTable(table, index);
		}
	}

	/**
	 * Resolves a property value either from the vanilla {@code propertyValues}
	 * array (when property-map elimination is disabled or the table is not
	 * yet installed) or from the shared table.
	 */
	public static Comparable<?> resolvePropertyValue(Comparable<?>[] legacyValues, StateTable<?> table, int tableIndex, int propertyIndex) {
		if (legacyValues != null) {
			return legacyValues[propertyIndex];
		}
		int valueIndex = table.valueIndexAt(tableIndex, propertyIndex);
		return table.properties()[propertyIndex].getPossibleValues().get(valueIndex);
	}
}
