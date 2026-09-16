package dev.vulkanperf.client;

import dev.vulkanperf.VulkanPerf;
import dev.vulkanperf.client.backend.BackendGuard;
import dev.vulkanperf.client.extras.ExtrasRuntime;
import dev.vulkanperf.client.imfast.ImFastRuntime;
import dev.vulkanperf.client.power.PowerController;
import dev.vulkanperf.config.PerfConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class VulkanPerfClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		PerfConfig.load();
		ImFastRuntime.syncFromConfig();
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			BackendGuard.log();
			PowerController.tick(client);
			ExtrasRuntime.tick();
		});
		VulkanPerf.LOGGER.info("vulkan-perf client ready");
	}
}
