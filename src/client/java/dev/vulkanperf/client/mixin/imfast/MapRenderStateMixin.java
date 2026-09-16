package dev.vulkanperf.client.mixin.imfast;

import dev.vulkanperf.client.imfast.map.MapRenderStateExtension;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MapRenderState.class)
public abstract class MapRenderStateMixin implements MapRenderStateExtension {
	@Unique
	private @Nullable Identifier vulkanperf$atlasTextureId;
	@Unique
	private int vulkanperf$atlasX;
	@Unique
	private int vulkanperf$atlasY;

	@Override
	public @Nullable Identifier vulkanperf$getAtlasTextureId() {
		return this.vulkanperf$atlasTextureId;
	}

	@Override
	public void vulkanperf$setAtlasTextureId(@Nullable Identifier id) {
		this.vulkanperf$atlasTextureId = id;
	}

	@Override
	public int vulkanperf$getAtlasX() {
		return this.vulkanperf$atlasX;
	}

	@Override
	public void vulkanperf$setAtlasX(int x) {
		this.vulkanperf$atlasX = x;
	}

	@Override
	public int vulkanperf$getAtlasY() {
		return this.vulkanperf$atlasY;
	}

	@Override
	public void vulkanperf$setAtlasY(int y) {
		this.vulkanperf$atlasY = y;
	}
}
