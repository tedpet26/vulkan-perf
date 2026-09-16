package dev.vulkanperf.client.mixin.imfast;

import com.mojang.renderpearl.api.device.GpuDevice;
import dev.vulkanperf.client.imfast.ImFastRuntime;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftImFastInitMixin {
	@Inject(
		method = "<init>",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;initRenderer(Lcom/mojang/renderpearl/api/device/GpuDevice;)V", shift = At.Shift.AFTER)
	)
	private void vulkanperf$imfastRendererInit(CallbackInfo ci) {
		GpuDevice device = com.mojang.blaze3d.systems.RenderSystem.getDevice();
		ImFastRuntime.onRenderSystemInit(device);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void vulkanperf$imfastLateInit(CallbackInfo ci) {
		ImFastRuntime.lateInit();
	}

	@Inject(method = "setLevel", at = @At("HEAD"))
	private void vulkanperf$imfastLevelChange(CallbackInfo ci) {
		ImFastRuntime.onLevelChange();
	}
}
