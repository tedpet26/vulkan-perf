package dev.vulkanperf.client.mixin.imfast;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import dev.vulkanperf.client.imfast.ImFastRuntime;
import dev.vulkanperf.client.imfast.gui.AnimatedItemAtlas;
import dev.vulkanperf.client.imfast.gui.GuiRendererExtension;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererItemBatchMixin implements GuiRendererExtension {
	@Shadow
	@Final
	private GuiRenderState renderState;

	@Shadow
	@Final
	private FeatureRenderDispatcher featureRenderDispatcher;

	@Shadow
	private int cachedGuiScale;

	@Unique
	private @Nullable AnimatedItemAtlas vulkanperf$animatedAtlas;
	@Unique
	private boolean vulkanperf$useAnimatedAtlas;

	@Inject(
		method = "prepareItemElements",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;prepareItemAtlas(Ljava/util/Set;I)Lnet/minecraft/client/gui/render/GuiItemAtlas;")
	)
	private void vulkanperf$prepareAnimatedAtlas(CallbackInfo ci) {
		if (!ImFastRuntime.batchAnimatedItemUpdates()) {
			this.vulkanperf$useAnimatedAtlas = false;
			return;
		}
		Set<Object> animatedIds = new ObjectOpenHashSet<>();
		this.renderState.forEachItem(item -> {
			TrackingItemStackRenderState state = item.itemStackRenderState();
			if (item.oversizedItemBounds() == null && state.isAnimated()) {
				animatedIds.add(state.getModelIdentity());
			}
		});
		this.vulkanperf$useAnimatedAtlas = this.vulkanperf$prepareAnimated(animatedIds, GuiRenderer.DEFAULT_ITEM_SIZE * this.cachedGuiScale);
	}

	@ModifyReceiver(
		method = "lambda$prepareItemElements$0",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiItemAtlas;getOrUpdate(Lnet/minecraft/client/renderer/item/TrackingItemStackRenderState;)Lnet/minecraft/client/gui/render/GuiItemAtlas$SlotView;")
	)
	private GuiItemAtlas vulkanperf$routeAnimated(GuiItemAtlas atlas, TrackingItemStackRenderState item) {
		if (this.vulkanperf$useAnimatedAtlas && this.vulkanperf$animatedAtlas != null && item.isAnimated()) {
			return this.vulkanperf$animatedAtlas;
		}
		return atlas;
	}

	@Inject(method = "endFrame", at = @At("RETURN"))
	private void vulkanperf$endAnimatedFrame(CallbackInfo ci) {
		if (this.vulkanperf$animatedAtlas != null) {
			this.vulkanperf$animatedAtlas.endFrame();
		}
	}

	@Inject(method = "invalidateItemAtlas", at = @At("RETURN"))
	private void vulkanperf$invalidateAnimated(CallbackInfo ci) {
		this.vulkanperf$closeAnimated();
	}

	@Inject(method = "close", at = @At("RETURN"))
	private void vulkanperf$closeAnimatedOnClose(CallbackInfo ci) {
		this.vulkanperf$closeAnimated();
	}

	@Unique
	private boolean vulkanperf$prepareAnimated(Set<Object> animatedItems, int slotTextureSize) {
		if (animatedItems.size() < 3) {
			return false;
		}
		int size = AnimatedItemAtlas.textureSizeFor(slotTextureSize, animatedItems.size());
		if (this.vulkanperf$animatedAtlas == null
			|| size > this.vulkanperf$animatedAtlas.textureSize()
			|| size < this.vulkanperf$animatedAtlas.textureSize() / 4) {
			this.vulkanperf$closeAnimated();
			this.vulkanperf$animatedAtlas = new AnimatedItemAtlas(this.featureRenderDispatcher, size, slotTextureSize);
		}
		return this.vulkanperf$animatedAtlas.tryPrepareFor(animatedItems);
	}

	@Unique
	private void vulkanperf$closeAnimated() {
		if (this.vulkanperf$animatedAtlas != null) {
			this.vulkanperf$animatedAtlas.close();
			this.vulkanperf$animatedAtlas = null;
		}
	}

	@Override
	public @Nullable AnimatedItemAtlas vulkanperf$animatedItemAtlas() {
		return this.vulkanperf$animatedAtlas;
	}
}
