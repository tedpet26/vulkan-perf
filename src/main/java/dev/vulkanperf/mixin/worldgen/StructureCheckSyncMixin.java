package dev.vulkanperf.mixin.worldgen;

import java.util.Collections;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;

/**
 * The structure-locator caches are plain maps written from whichever thread generates the chunk;
 * parallel worldgen needs them synchronized. Structure lookups stay cheap: the synchronization
 * only covers map mutations and point lookups.
 */
@Mixin(StructureCheck.class)
public abstract class StructureCheckSyncMixin {
	@Mutable
	@Shadow
	@Final
	private Long2ObjectMap<Object2IntMap<Structure>> loadedChunks;

	@Mutable
	@Shadow
	@Final
	private java.util.Map<Structure, it.unimi.dsi.fastutil.longs.Long2BooleanMap> featureChecks;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void vp$syncCaches(
		net.minecraft.world.level.chunk.storage.ChunkScanAccess storageAccess,
		net.minecraft.core.RegistryAccess registryAccess,
		net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager structureTemplateManager,
		net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension,
		ChunkGenerator chunkGenerator,
		RandomState randomState,
		net.minecraft.world.level.LevelHeightAccessor heightAccessor,
		net.minecraft.world.level.biome.BiomeSource biomeSource,
		long seed,
		com.mojang.datafixers.DataFixer fixerUpper,
		CallbackInfo ci
	) {
		this.loadedChunks = it.unimi.dsi.fastutil.longs.Long2ObjectMaps.synchronize(this.loadedChunks);
		this.featureChecks = Collections.synchronizedMap(this.featureChecks);
	}
}
