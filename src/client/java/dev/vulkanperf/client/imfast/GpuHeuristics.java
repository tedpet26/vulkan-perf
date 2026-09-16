package dev.vulkanperf.client.imfast;

import com.mojang.renderpearl.api.device.DeviceInfo;
import dev.vulkanperf.VulkanPerf;

import java.util.Locale;
import java.util.Set;

/**
 * Vendor/backend heuristics mirrored from ImmediatelyFast's runtime config, retargeted to
 * RenderPearl {@link DeviceInfo}. GL-only tricks stay off on Vulkan.
 */
public final class GpuHeuristics {
	private GpuHeuristics() {
	}

	public record Result(boolean avoidRedundantFramebufferSwitching, boolean fixSlowBufferUploadOnAppleGpu) {
	}

	public static Result evaluate(DeviceInfo info, boolean vulkan) {
		if (info == null || vulkan) {
			return new Result(false, false);
		}
		String vendor = safe(info.vendorName()).toLowerCase(Locale.ROOT);
		String model = safe(info.name());
		String backend = safe(info.backendName());
		boolean opengl = backend.equalsIgnoreCase("OpenGL") || backend.toLowerCase(Locale.ROOT).contains("opengl");
		boolean apple = vendor.startsWith("apple");
		boolean intel = vendor.startsWith("intel");
		Set<String> extensions = info.underlyingExtensions() == null ? Set.of() : info.underlyingExtensions();

		boolean appleUpload = apple && opengl
			&& !(extensions.contains("GL_ARB_direct_state_access") || extensions.contains("GL_ARB_buffer_storage"));
		boolean skipFramebuffer = opengl;
		if (skipFramebuffer && intel && (model.contains("UHD Graphics") || model.contains("Xe Graphics"))) {
			VulkanPerf.LOGGER.warn("Intel UHD/Xe OpenGL detected; disabling redundant framebuffer skip");
			skipFramebuffer = false;
		}
		return new Result(skipFramebuffer, appleUpload);
	}

	private static String safe(String value) {
		return value == null ? "" : value;
	}
}
