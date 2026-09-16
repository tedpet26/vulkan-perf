package dev.vulkanperf.memory.fastmap;

/**
 * Duck interface implemented by the {@code StateHolder} mixin so the shared
 * {@link StateTable} and this holder's slot within it can be attached without
 * touching vanilla's per-state neighbor array field.
 */
public interface StateTableHolder<S> {
	void vulkanperf$installStateTable(StateTable<S> table, int index);
}
