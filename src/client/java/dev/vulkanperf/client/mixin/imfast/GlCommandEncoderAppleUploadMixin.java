package dev.vulkanperf.client.mixin.imfast;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.backend.opengl.DirectStateAccess;
import dev.vulkanperf.client.imfast.ImFastRuntime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.ByteBuffer;

@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlCommandEncoder")
public abstract class GlCommandEncoderAppleUploadMixin {
	@Redirect(
		method = "writeToBuffer",
		at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/DirectStateAccess;bufferSubData(IJLjava/nio/ByteBuffer;I)V")
	)
	private void vulkanperf$appleFullBufferUpload(
		DirectStateAccess dsa,
		int buffer,
		long offset,
		ByteBuffer data,
		int usage,
		@Local(argsOnly = true) GpuBufferSlice slice
	) {
		if (ImFastRuntime.fixSlowBufferUploadOnAppleGpu() && offset == 0L && slice.length() == slice.buffer().size()) {
			dsa.bufferData(buffer, data, usage);
		} else {
			dsa.bufferSubData(buffer, offset, data, usage);
		}
	}
}
