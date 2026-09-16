package dev.vulkanperf.client.mixin.imfast;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.vulkanperf.client.imfast.ImFastRuntime;
import dev.vulkanperf.client.mixin.imfast.accessors.RenderTypeVertexBuilderInvoker;
import net.minecraft.client.renderer.feature.TextFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.client.renderer.feature.TextFeatureRenderer$GlyphRenderer")
public abstract class TextGlyphLookupMixin {
	@Unique
	private RenderType vulkanperf$lastType;
	@Unique
	private VertexConsumer vulkanperf$lastConsumer;

	@Redirect(
		method = "acceptRenderable",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/feature/TextFeatureRenderer;getVertexBuilder(Lnet/minecraft/client/renderer/rendertype/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
		)
	)
	private VertexConsumer vulkanperf$reuseVertexBuilder(TextFeatureRenderer renderer, RenderType renderType) {
		if (!ImFastRuntime.fastTextLookup()) {
			return ((RenderTypeVertexBuilderInvoker) renderer).vulkanperf$getVertexBuilder(renderType);
		}
		if (this.vulkanperf$lastType != renderType) {
			this.vulkanperf$lastType = renderType;
			this.vulkanperf$lastConsumer = ((RenderTypeVertexBuilderInvoker) renderer).vulkanperf$getVertexBuilder(renderType);
		}
		return this.vulkanperf$lastConsumer;
	}
}
