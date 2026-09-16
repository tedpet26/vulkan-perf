package dev.vulkanperf.client.backend;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.device.DeviceInfo;
import com.mojang.renderpearl.api.device.GpuDevice;
import dev.vulkanperf.VulkanPerf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.PreferredGraphicsApi;

public final class BackendGuard {
	private static boolean logged;

	private BackendGuard() {
	}

	public static void log() {
		if (logged) {
			return;
		}
		try {
			Minecraft client = Minecraft.getInstance();
			String surface = "none";
			if (client != null && client.windowSurface() != null) {
				surface = client.windowSurface().getClass().getName();
			}
			GpuDevice device = RenderSystem.tryGetDevice();
			String deviceName = device == null ? "none" : device.getClass().getName();
			String backend = backendName(device);
			boolean vulkan = isVulkan();
			VulkanPerf.LOGGER.info("Blaze3D surface: {} device: {} backend: {} (vulkan={})", surface, deviceName, backend, vulkan);
			logged = true;
		} catch (IllegalStateException early) {
			VulkanPerf.LOGGER.debug("GPU device not ready yet");
		}
	}

	public static boolean isVulkan() {
		try {
			GpuDevice device = RenderSystem.tryGetDevice();
			String backend = backendName(device);
			if (backend.contains("vulkan") || backend.contains("vk")) {
				return true;
			}
			if (backend.contains("opengl") || backend.contains("gl")) {
				return false;
			}
			Minecraft client = Minecraft.getInstance();
			if (client != null && client.options != null) {
				return client.options.preferredGraphicsBackend().get() == PreferredGraphicsApi.VULKAN;
			}
			return false;
		} catch (IllegalStateException | NullPointerException early) {
			return false;
		}
	}

	private static String backendName(GpuDevice device) {
		if (device == null) {
			return "none";
		}
		DeviceInfo info = device.getDeviceInfo();
		if (info == null || info.backendName() == null) {
			return device.getClass().getName().toLowerCase();
		}
		return info.backendName().toLowerCase();
	}
}
