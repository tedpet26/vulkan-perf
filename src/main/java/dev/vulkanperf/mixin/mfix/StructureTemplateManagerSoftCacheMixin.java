package dev.vulkanperf.mixin.mfix;

import java.util.Map;
import java.util.Optional;

import com.google.common.cache.CacheBuilder;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * Loaded structure templates are pinned for the lifetime of the server; big datapack servers
 * touch thousands of them once and never again. Soft references let the GC reclaim cold
 * templates under pressure while hot ones stay exact (reloads re-read from the source).
 */
@Mixin(StructureTemplateManager.class)
public abstract class StructureTemplateManagerSoftCacheMixin {
	@Mutable
	@Shadow
	@Final
	private Map<Identifier, Optional<StructureTemplate>> structureRepository;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void vp$softCache(
		net.minecraft.server.packs.resources.ResourceManager resourceManager,
		net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess storage,
		com.mojang.datafixers.DataFixer fixerUpper,
		net.minecraft.core.HolderGetter<net.minecraft.world.level.block.Block> blockLookup,
		CallbackInfo ci
	) {
		com.google.common.cache.Cache<Identifier, Optional<StructureTemplate>> soft =
			CacheBuilder.newBuilder().softValues().build();
		this.structureRepository = soft.asMap();
	}
}
