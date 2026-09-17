package dev.vulkanperf.client.particles;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.culling.Frustum;

/**
 * Per-frame culling context captured at {@code ParticleEngine#extract}: camera position and the
 * squared culling range derived from the client render distance.
 */
public final class ParticleCullingState {
	private static double cameraX;
	private static double cameraY;
	private static double cameraZ;
	private static double maxDistanceSquared = Double.MAX_VALUE;

	private ParticleCullingState() {
	}

	public static void beginFrame(Frustum frustum, Camera camera, double rangeMultiplier) {
		cameraX = camera.position().x;
		cameraY = camera.position().y;
		cameraZ = camera.position().z;
		net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
		double blocks = 32.0 * 16.0 * rangeMultiplier;
		if (minecraft.options != null) {
			blocks = minecraft.options.getEffectiveRenderDistance() * 16.0 * rangeMultiplier;
		}
		maxDistanceSquared = blocks * blocks;
	}

	public static double cameraDistanceSquared(double x, double y, double z) {
		double dx = x - cameraX;
		double dy = y - cameraY;
		double dz = z - cameraZ;
		return dx * dx + dy * dy + dz * dz;
	}

	public static boolean withinRenderDistance(double x, double y, double z) {
		return cameraDistanceSquared(x, y, z) <= maxDistanceSquared;
	}
}
