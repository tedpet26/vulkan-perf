package dev.vulkanperf.mixin.memory.fastmap;

import dev.vulkanperf.config.PerfConfig;
import dev.vulkanperf.memory.fastmap.StateTable;
import dev.vulkanperf.memory.fastmap.StateTableHolder;
import dev.vulkanperf.memory.fastmap.StateTableInstaller;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Replaces vanilla's per-state {@code S[][] neighbors} array (and, optionally,
 * the {@code Comparable<?>[] propertyValues} array) with a single shared
 * {@link StateTable} per block/property-set, installed by
 * {@code StateDefinitionFastMapMixin}.
 *
 * <p>Runs at a slightly elevated priority so it applies before any other
 * mixin might touch these members.</p>
 */
@Mixin(value = StateHolder.class, priority = 900)
public abstract class StateHolderFastMapMixin<O, S> implements StateTableHolder<S> {

	@Shadow
	@Final
	@Mutable
	private Comparable<?>[] propertyValues;

	@Shadow
	@Final
	protected O owner;

	@Shadow
	public abstract boolean isSingletonState();

	@Unique
	private StateTable<S> vulkanperf$table;

	@Unique
	private int vulkanperf$tableIndex;

	/**
	 * @author vulkanperf
	 * @reason Replace the vanilla neighbor-array lookup with a shared state
	 * table lookup; the neighbor array is never populated when this mixin's
	 * package is active (see StateDefinitionFastMapMixin).
	 */
	@Overwrite
	private <T extends Comparable<T>, V extends T> S setValueInternal(Property<T> property, int propertyIndex, V value) {
		int valueIndex = property.getInternalIndex(value);
		if (valueIndex < 0) {
			throw new IllegalArgumentException("Cannot set property " + property + " to " + value + " on " + this.owner + ", it is not an allowed value");
		}
		return this.vulkanperf$table.neighbor(this.vulkanperf$tableIndex, propertyIndex, valueIndex);
	}

	/**
	 * @author vulkanperf
	 * @reason Vanilla neighbor arrays are replaced by the shared state table;
	 * singleton states (no properties) never read the table, so there is
	 * nothing left to store here.
	 */
	@Overwrite
	void initializeNeighbors(S[][] neighbors) {
		if (!this.isSingletonState()) {
			throw new UnsupportedOperationException(
					"Neighbor arrays are replaced by vulkan-perf. initializeNeighbors should only run for singleton states."
			);
		}
	}

	@Redirect(
			method = {"getNullableValue", "lambda$getValues$0"},
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/world/level/block/state/StateHolder;propertyValues:[Ljava/lang/Comparable;",
					opcode = Opcodes.GETFIELD,
					args = "array=get"
			)
	)
	private Comparable<?> vulkanperf$readPropertyValue(Comparable<?>[] values, int index) {
		return StateTableInstaller.resolvePropertyValue(values, this.vulkanperf$table, this.vulkanperf$tableIndex, index);
	}

	@Override
	public void vulkanperf$installStateTable(StateTable<S> table, int index) {
		this.vulkanperf$table = table;
		this.vulkanperf$tableIndex = index;
		if (PerfConfig.get().memory.fastMapPropertyMap) {
			this.propertyValues = null;
		}
	}
}
