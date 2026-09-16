package dev.vulkanperf.mixin.memory.fastmap;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.vulkanperf.config.PerfConfig;
import dev.vulkanperf.memory.fastmap.StateTableInstaller;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Intercepts the two places {@link StateDefinition} normally builds vanilla
 * neighbor tables and replaces them with a shared {@code StateTable} instead.
 * This mixin is only applied (see {@code VulkanPerfMixinPlugin}) when the
 * FastMap neighbor lookup option is enabled, so the vanilla path is left
 * completely untouched when that option is off.
 */
@Mixin(StateDefinition.class)
public abstract class StateDefinitionFastMapMixin {

	@Redirect(
			method = "createMultiPropertyStates",
			at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V")
	)
	private static <O, S extends StateHolder<O, S>> void vulkanperf$installMultiStateTable(
			Map<List<Comparable<?>>, S> statesByValues, BiConsumer<?, ?> unusedConsumer
	) {
		StateTableInstaller.install(statesByValues.values(), PerfConfig.get().memory.compactFastMap);
	}

	@Redirect(
			method = "createSinglePropertyStates(Ljava/lang/Object;Lnet/minecraft/world/level/block/state/StateDefinition$Factory;Lnet/minecraft/world/level/block/state/properties/Property;)Lcom/google/common/collect/ImmutableList;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/StateHolder;initializeNeighbors([[Ljava/lang/Object;)V")
	)
	private static <S> void vulkanperf$skipSingleNeighborArray(StateHolder<?, ?> state, S[][] neighbors) {
		// The array of size-1 neighbor rows is cheap to build (one property), but storing
		// it is unnecessary once the shared state table is installed right below.
	}

	@WrapOperation(
			method = "createSinglePropertyStates(Ljava/lang/Object;Lnet/minecraft/world/level/block/state/StateDefinition$Factory;Lnet/minecraft/world/level/block/state/properties/Property;)Lcom/google/common/collect/ImmutableList;",
			at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList$Builder;build()Lcom/google/common/collect/ImmutableList;")
	)
	private static <O, S extends StateHolder<O, S>> ImmutableList<S> vulkanperf$installSingleStateTable(
			ImmutableList.Builder<S> builder, Operation<ImmutableList<S>> original
	) {
		ImmutableList<S> built = original.call(builder);
		StateTableInstaller.install(built, PerfConfig.get().memory.compactFastMap);
		return built;
	}
}
