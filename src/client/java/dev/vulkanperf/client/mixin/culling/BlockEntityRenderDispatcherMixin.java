package dev.vulkanperf.client.mixin.culling;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {
	@Inject(method = "tryExtractRenderState", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$distanceCull(BlockEntity blockEntity, float partialTick, ModelFeatureRenderer.CrumblingOverlay overlay, boolean globallyRendered, CallbackInfoReturnable<BlockEntityRenderState> cir) {
		if (!PerfConfig.get().culling.blockEntities || globallyRendered || blockEntity == null) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		if (client.gameRenderer == null) {
			return;
		}
		Camera camera = client.gameRenderer.mainCamera();
		var pos = blockEntity.getBlockPos();
		double dx = pos.getX() + 0.5 - camera.position().x;
		double dy = pos.getY() + 0.5 - camera.position().y;
		double dz = pos.getZ() + 0.5 - camera.position().z;
		double range = client.options.getEffectiveRenderDistance() * 16.0;
		if (dx * dx + dy * dy + dz * dz > range * range) {
			cir.setReturnValue(null);
		}
	}
}
