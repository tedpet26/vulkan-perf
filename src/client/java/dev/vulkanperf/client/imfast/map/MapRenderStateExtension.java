package dev.vulkanperf.client.imfast.map;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * Implemented on {@code net.minecraft.client.renderer.state.MapRenderState} via mixin to stash the
 * resolved atlas texture + offset for the map being rendered this frame, so the geometry-emitting
 * mixins can rewrite UVs to point into the shared atlas sheet instead of a per-map texture.
 */
public interface MapRenderStateExtension {
	@Nullable
	Identifier vulkanperf$getAtlasTextureId();

	void vulkanperf$setAtlasTextureId(@Nullable Identifier id);

	int vulkanperf$getAtlasX();

	void vulkanperf$setAtlasX(int x);

	int vulkanperf$getAtlasY();

	void vulkanperf$setAtlasY(int y);
}
