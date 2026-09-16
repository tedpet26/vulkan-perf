package dev.vulkanperf.client.mixin.imfast;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.vulkanperf.client.imfast.ImFastRuntime;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Text quads are already sorted by glyph atlas + overlay order; vertex sorting on the
 * polygon-offset / grayscale see-through text pipelines is wasted work.
 */
@Mixin(RenderSetup.RenderSetupBuilder.class)
public abstract class RenderTypesTextSortMixin {
	@Shadow
	@Final
	private RenderPipeline pipeline;

	@Inject(method = "sortOnUpload", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$skipTextSorting(CallbackInfoReturnable<RenderSetup.RenderSetupBuilder> cir) {
		if (!ImFastRuntime.skipTextTranslucencySorting()) {
			return;
		}
		Identifier location = this.pipeline.getLocation();
		if (location == null) {
			return;
		}
		String path = location.getPath();
		if ("pipeline/text_polygon_offset".equals(path)
			|| "pipeline/text_grayscale_polygon_offset".equals(path)
			|| "pipeline/text_grayscale_see_through".equals(path)) {
			cir.setReturnValue((RenderSetup.RenderSetupBuilder) (Object) this);
		}
	}
}
