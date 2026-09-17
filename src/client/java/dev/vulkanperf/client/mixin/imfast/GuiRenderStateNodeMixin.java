package dev.vulkanperf.client.mixin.imfast;

import dev.vulkanperf.client.imfast.GuiIntersectionIndex;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Mirrors element adds into {@link GuiIntersectionIndex} so the intersection scan can be
 * skipped when the added bounds fall outside the list's union. Only the four lists vanilla
 * actually queries (element/item/text/PiP) are tracked; glyphs bypass the scan entirely.
 */
@Mixin(targets = "net.minecraft.client.renderer.state.gui.GuiRenderState$Node")
public abstract class GuiRenderStateNodeMixin {
	@Shadow
	public List<GuiElementRenderState> elementStates;
	@Shadow
	public List<GuiItemRenderState> itemStates;
	@Shadow
	public List<GuiTextRenderState> textStates;
	@Shadow
	public List<PictureInPictureRenderState> picturesInPictureStates;

	@Inject(method = "addItem(Lnet/minecraft/client/renderer/state/gui/GuiItemRenderState;)V", at = @At("TAIL"))
	private void vulkanperf$trackItem(GuiItemRenderState item, CallbackInfo ci) {
		ScreenRectangle bounds = item.bounds();
		if (bounds != null) {
			GuiIntersectionIndex.track(this.itemStates, bounds);
		}
	}

	@Inject(method = "addText(Lnet/minecraft/client/renderer/state/gui/GuiTextRenderState;)V", at = @At("TAIL"))
	private void vulkanperf$trackText(GuiTextRenderState text, CallbackInfo ci) {
		ScreenRectangle bounds = text.bounds();
		if (bounds != null) {
			GuiIntersectionIndex.track(this.textStates, bounds);
		}
	}

	@Inject(method = "addGuiElement(Lnet/minecraft/client/renderer/state/gui/GuiElementRenderState;)V", at = @At("TAIL"))
	private void vulkanperf$trackElement(GuiElementRenderState element, CallbackInfo ci) {
		ScreenRectangle bounds = element.bounds();
		if (bounds != null) {
			GuiIntersectionIndex.track(this.elementStates, bounds);
		}
	}

	@Inject(method = "addPicturesInPictureState(Lnet/minecraft/client/renderer/state/gui/pip/PictureInPictureRenderState;)V", at = @At("TAIL"))
	private void vulkanperf$trackPiP(PictureInPictureRenderState pip, CallbackInfo ci) {
		ScreenRectangle bounds = pip.bounds();
		if (bounds != null) {
			GuiIntersectionIndex.track(this.picturesInPictureStates, bounds);
		}
	}
}
