package dev.vulkanperf.client.mixin.particles;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;

/**
 * Particles re-query the light engine every frame for unchanged positions; a per-particle
 * per-tick cache drops the world lookup to once per game tick.
 */
@Mixin(Particle.class)
public abstract class ParticleLightCacheMixin {
	@Shadow
	protected ClientLevel level;

	@Shadow
	protected double x;

	@Shadow
	protected double y;

	@Shadow
	protected double z;

	@Unique
	private long vp$lightCacheTick = Long.MIN_VALUE;

	@Unique
	private int vp$lightCacheValue;

	@Unique
	private BlockPos vp$lightCachePos;

	/**
	 * @reason cache lightmap per tick
	 * @author vulkan-perf
	 */
	@Overwrite
	protected int getLightCoords(final float a) {
		BlockPos pos = BlockPos.containing(this.x, this.y, this.z);
		if (!PerfConfig.get().particles.lightCache) {
			return this.level.hasChunkAt(pos) ? net.minecraft.util.LightCoordsUtil.getLightCoords(this.level, pos) : 15728640;
		}
		long tick = this.level.getGameTime();
		if (tick == this.vp$lightCacheTick && pos.equals(this.vp$lightCachePos)) {
			return this.vp$lightCacheValue;
		}
		int value = this.level.hasChunkAt(pos) ? net.minecraft.util.LightCoordsUtil.getLightCoords(this.level, pos) : 15728640;
		this.vp$lightCacheTick = tick;
		this.vp$lightCachePos = pos.immutable();
		this.vp$lightCacheValue = value;
		return value;
	}
}
