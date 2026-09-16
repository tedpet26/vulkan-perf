package dev.vulkanperf.client.extras;

import net.minecraft.resources.Identifier;

import java.util.WeakHashMap;

public final class AnimationSprites {
	private static final WeakHashMap<Object, Identifier> IDS = new WeakHashMap<>();

	private AnimationSprites() {
	}

	public static void bind(Object state, Identifier id) {
		if (state != null && id != null) {
			IDS.put(state, id);
		}
	}

	public static Identifier id(Object state) {
		return state == null ? null : IDS.get(state);
	}
}
