package dev.vulkanperf.mixin.worldgen;

import java.util.Collections;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * Structure template lists are read while placed and written while loaded; parallel worldgen
 * placing the same template from two threads races on them (MC-271899). Wrapping in
 * synchronized lists makes template placement thread-safe at negligible cost.
 */
@Mixin(StructureTemplate.class)
public abstract class StructureTemplateSyncMixin {
	@Mutable
	@Shadow
	@Final
	private List<StructureTemplate.Palette> palettes;

	@Mutable
	@Shadow
	@Final
	private List<StructureTemplate.StructureEntityInfo> entityInfoList;

	@Inject(method = "<init>()V", at = @At("TAIL"))
	private void vp$syncLists(CallbackInfo ci) {
		this.palettes = Collections.synchronizedList(this.palettes);
		this.entityInfoList = Collections.synchronizedList(this.entityInfoList);
	}
}
