package dev.vulkanperf.client.mixin.imfast;

import com.mojang.blaze3d.systems.ScissorState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(ScissorState.class)
public abstract class ScissorStateMixin {
	@Shadow
	private boolean enabled;
	@Shadow
	private int x;
	@Shadow
	private int y;
	@Shadow
	private int width;
	@Shadow
	private int height;

	/**
	 * @author vulkan-perf
	 * @reason Render-type group merging keys scissor rectangles; vanilla ScissorState has identity equality only.
	 */
	@Overwrite
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ScissorStateMixin that)) {
			return false;
		}
		return this.enabled == that.enabled
			&& this.x == that.x
			&& this.y == that.y
			&& this.width == that.width
			&& this.height == that.height;
	}

	/**
	 * @author vulkan-perf
	 * @reason Pair with {@link #equals(Object)} so scissor rectangles can be used as merge keys.
	 */
	@Overwrite
	public int hashCode() {
		return Objects.hash(this.enabled, this.x, this.y, this.width, this.height);
	}
}
