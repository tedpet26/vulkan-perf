package dev.vulkanperf.mixin.worldgen;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.resources.Identifier;

/**
 * {@code Identifier#toString} rebuilds {@code namespace + ":" + path} on every call; hot paths
 * (logging, datapack loading, registry iteration) cache the composed string on the instance.
 */
@Mixin(Identifier.class)
public abstract class IdentifierToStringMixin {
	@Shadow
	@Final
	private String namespace;

	@Shadow
	@Final
	private String path;

	@Unique
	private String vp$cachedToString;

	/**
	 * @reason cache the composed string
	 * @author vulkan-perf
	 */
	@Overwrite
	public String toString() {
		String s = this.vp$cachedToString;
		if (s == null) {
			s = this.namespace + ":" + this.path;
			this.vp$cachedToString = s;
		}
		return s;
	}
}
