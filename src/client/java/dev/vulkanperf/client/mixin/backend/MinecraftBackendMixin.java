package dev.vulkanperf.client.mixin.backend;

import dev.vulkanperf.client.backend.BackendGuard;
import dev.vulkanperf.client.power.PowerController;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftBackendMixin {
	@Inject(method = "runTick", at = @At("HEAD"))
	private void vulkanperf$logBackend(CallbackInfo ci) {
		BackendGuard.log();
	}

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$skipHiddenTicks(CallbackInfo ci) {
		if (PowerController.shouldSkipTick((Minecraft) (Object) this)) {
			ci.cancel();
		}
	}
}
