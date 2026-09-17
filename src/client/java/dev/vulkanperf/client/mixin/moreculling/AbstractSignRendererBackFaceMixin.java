package dev.vulkanperf.client.mixin.moreculling;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

/**
 * Sign text back-face culling (MoreCulling class): the text plane of a sign is only readable
 * from one side, so when the camera sits behind a wall/hanging sign its front text (and vice
 * versa) is skipped at extract time instead of being submitted, sorted and clipped. Standing
 * signs are left untouched — their rotation-to-facing mapping is ambiguous enough that a wrong
 * cull would visibly delete text.
 */
@Mixin(AbstractSignRenderer.class)
public abstract class AbstractSignRendererBackFaceMixin {

	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private <S extends SignRenderState> void vp$cullBackFaceText(
		SignBlockEntity blockEntity,
		S state,
		float partialTicks,
		Vec3 cameraPosition,
		ModelFeatureRenderer.@org.jetbrains.annotations.Nullable CrumblingOverlay breakProgress,
		CallbackInfo ci
	) {
		if (!PerfConfig.get().moreculling.signTextBackFace) {
			return;
		}
		BlockState blockState = blockEntity.getBlockState();
		if (!blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
			return;
		}
		Direction facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
		double side = cameraPosition.subtract(Vec3.atCenterOf(blockEntity.getBlockPos()))
			.dot(Vec3.atLowerCornerOf(facing.getUnitVec3i()));
		if (side < -0.4) {
			state.frontText = null;
		} else if (side > 0.4) {
			state.backText = null;
		}
	}
}
