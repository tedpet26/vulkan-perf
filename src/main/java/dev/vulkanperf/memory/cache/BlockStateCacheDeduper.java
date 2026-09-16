package dev.vulkanperf.memory.cache;

import com.google.common.base.Suppliers;
import dev.vulkanperf.mixin.memory.accessors.ArrayVoxelShapeAccessor;
import dev.vulkanperf.mixin.memory.accessors.VoxelShapeAccessor;
import dev.vulkanperf.memory.hash.ArrayShapeEquivalence;
import dev.vulkanperf.memory.hash.ShapeEquivalence;
import it.unimi.dsi.fastutil.booleans.BooleanArrays;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Deduplicates the {@code VoxelShape} and {@code boolean[]} arrays stashed in
 * each blockstate's private {@code Cache} object. Many blocks compute
 * structurally identical collision shapes / face-sturdiness tables (e.g. a
 * full cube), so interning them by structural equality rather than identity
 * avoids keeping thousands of equivalent copies alive.
 *
 * <p>The {@code Cache} inner class is private, so it cannot be referenced by
 * type from this compilation unit; a small amount of reflection is used to
 * reach the field once and cast the result to the public {@link ShapeCacheView}
 * a sibling mixin makes that private class implement.</p>
 */
public final class BlockStateCacheDeduper {
	private static final Map<ArrayVoxelShapeAccessor, ArrayVoxelShapeAccessor> COLLISION_SHAPE_POOL =
			new Object2ObjectOpenCustomHashMap<>(ArrayShapeEquivalence.INSTANCE);
	private static final Map<boolean[], boolean[]> FACE_STURDY_POOL =
			new Object2ObjectOpenCustomHashMap<>(BooleanArrays.HASH_STRATEGY);

	// The cache being replaced for the current state; carried across the pre/post pair of
	// calls around BlockStateBase#initCache on the same thread so we can skip the pool lookup
	// entirely when the new cache is structurally identical to the one it is replacing.
	private static final ThreadLocal<ShapeCacheView> PREVIOUS_CACHE = new ThreadLocal<>();

	private static final Supplier<Function<BlockState, ShapeCacheView>> CACHE_VIEW = Suppliers.memoize(BlockStateCacheDeduper::createCacheViewLookup);

	private BlockStateCacheDeduper() {
	}

	public static void captureBefore(BlockState state) {
		PREVIOUS_CACHE.set(CACHE_VIEW.get().apply(state));
	}

	public static void deduplicateAfter(BlockState state) {
		ShapeCacheView current = CACHE_VIEW.get().apply(state);
		if (current != null) {
			ShapeCacheView previous = PREVIOUS_CACHE.get();
			deduplicateCollisionShape(current, previous);
			deduplicateFaceSturdy(current, previous);
		}
		PREVIOUS_CACHE.remove();
	}

	private static void deduplicateCollisionShape(ShapeCacheView current, @Nullable ShapeCacheView previous) {
		VoxelShape currentShape = current.vulkanperf$getCollisionShape();
		VoxelShape kept = currentShape;
		if (previous != null && ShapeEquivalence.INSTANCE.equals(previous.vulkanperf$getCollisionShape(), currentShape)) {
			kept = previous.vulkanperf$getCollisionShape();
		} else if (currentShape instanceof ArrayVoxelShapeAccessor accessor) {
			kept = (VoxelShape) COLLISION_SHAPE_POOL.computeIfAbsent(accessor, Function.identity());
		}
		mergeArrayShapeStorage(kept, currentShape);
		current.vulkanperf$setCollisionShape(kept);
	}

	/**
	 * Mods frequently cache the shapes we hand out in their own long-lived
	 * structures, so removing duplicates only from our own cache would leave
	 * those alive. Where possible, we instead point the discarded shape's own
	 * backing arrays at the kept shape's arrays, shrinking retained size
	 * without needing every caller to go through our pool.
	 */
	private static void mergeArrayShapeStorage(VoxelShape kept, VoxelShape discarded) {
		if (kept == discarded || !(kept instanceof ArrayVoxelShapeAccessor keptArray) || !(discarded instanceof ArrayVoxelShapeAccessor discardedArray)) {
			return;
		}
		discardedArray.vulkanperf$setXPoints(keptArray.vulkanperf$getXPoints());
		discardedArray.vulkanperf$setYPoints(keptArray.vulkanperf$getYPoints());
		discardedArray.vulkanperf$setZPoints(keptArray.vulkanperf$getZPoints());
		((VoxelShapeAccessor) discardedArray).vulkanperf$setShape(((VoxelShapeAccessor) keptArray).vulkanperf$getShape());
		((VoxelShapeAccessor) discardedArray).vulkanperf$setFaces(((VoxelShapeAccessor) keptArray).vulkanperf$getFaces());
	}

	private static void deduplicateFaceSturdy(ShapeCacheView current, @Nullable ShapeCacheView previous) {
		boolean[] currentArray = current.vulkanperf$getFaceSturdy();
		boolean[] kept;
		if (previous != null && Arrays.equals(previous.vulkanperf$getFaceSturdy(), currentArray)) {
			kept = previous.vulkanperf$getFaceSturdy();
		} else {
			kept = FACE_STURDY_POOL.computeIfAbsent(currentArray, Function.identity());
		}
		current.vulkanperf$setFaceSturdy(kept);
	}

	private static Function<BlockState, ShapeCacheView> createCacheViewLookup() {
		try {
			Field cacheField = BlockBehaviour.BlockStateBase.class.getDeclaredField("cache");
			cacheField.setAccessible(true);
			MethodHandle getter = MethodHandles.lookup().unreflectGetter(cacheField);
			return state -> {
				try {
					return (ShapeCacheView) getter.invoke((BlockBehaviour.BlockStateBase) state);
				} catch (Throwable throwable) {
					throw new RuntimeException("Failed to read blockstate shape cache", throwable);
				}
			};
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Failed to locate blockstate shape cache field", e);
		}
	}
}
