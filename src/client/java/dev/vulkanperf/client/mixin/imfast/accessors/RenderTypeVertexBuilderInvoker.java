package dev.vulkanperf.client.mixin.imfast.accessors;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderTypeFeatureRenderer.class)
public interface RenderTypeVertexBuilderInvoker {
	@Invoker("getVertexBuilder")
	VertexConsumer vulkanperf$getVertexBuilder(RenderType renderType);
}
