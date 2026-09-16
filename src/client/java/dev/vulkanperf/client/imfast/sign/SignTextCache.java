package dev.vulkanperf.client.imfast.sign;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import dev.vulkanperf.client.imfast.ImFastRuntime;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.block.entity.SignText;

import java.time.Duration;

public final class SignTextCache implements ResourceManagerReloadListener {
	public final SignTextAtlas atlas;
	public final RenderType renderType;
	public final GpuBufferSlice projection;
	public final Cache<SignText, SignTextAtlas.Slot> slots = CacheBuilder.newBuilder()
		.expireAfterAccess(Duration.ofSeconds(5L))
		.removalListener(notification -> {
			if (notification.getCause().equals(RemovalCause.EXPLICIT)) {
				return;
			}
			Object value = notification.getValue();
			if (value instanceof SignTextAtlas.Slot slot) {
				slot.markFree();
			}
		})
		.build();

	private final ProjectionMatrixBuffer projectionBuffer;

	public SignTextCache() {
		RenderSystem.assertOnRenderThread();
		this.atlas = new SignTextAtlas();
		this.renderType = RenderTypes.text(this.atlas.textureId());
		Projection ortho = new Projection();
		ortho.setupOrtho(-1000.0F, 1000.0F, ImFastRuntime.signAtlasSize(), ImFastRuntime.signAtlasSize(), true);
		this.projectionBuffer = new ProjectionMatrixBuffer("vulkanperf sign atlas");
		this.projection = this.projectionBuffer.getBuffer(ortho);
	}

	public void clear() {
		RenderSystem.assertOnRenderThread();
		this.slots.invalidateAll();
		this.atlas.clearAtlas();
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		this.clear();
	}
}
