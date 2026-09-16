package dev.vulkanperf.memory.fastmap;

/**
 * Defines how a single {@code Property} contributes to the flat index used by
 * {@link StateTable}. Implementations decide how much of the index space a
 * property's possible values occupy and how to move between "neighbor" values
 * of that property without touching any other property's slot.
 */
public interface PropertyIndexer {

	/**
	 * @param index      the current flat index into the state table
	 * @param valueIndex the internal index of the new value for this property
	 * @return the flat index with only this property's slot replaced, or a
	 * negative number if {@code valueIndex} is out of range
	 */
	int moveTo(int index, int valueIndex);

	/**
	 * @param valueIndex the internal index of a value of this property
	 * @return the contribution of this value to a flat index, such that summing
	 * the contributions of every property yields the index for that combination
	 */
	int baseOffset(int valueIndex);

	/**
	 * @return the number of index slots consumed by this property; multiplying
	 * this across all properties of a table gives the table size
	 */
	int span();

	/**
	 * @param index the flat index into the state table
	 * @return the internal value index this property has within {@code index}
	 */
	int extract(int index);
}
