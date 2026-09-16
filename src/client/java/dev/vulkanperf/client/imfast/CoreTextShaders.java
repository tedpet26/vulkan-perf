package dev.vulkanperf.client.imfast;

import com.mojang.renderpearl.api.pipeline.ShaderType;
import net.minecraft.resources.Identifier;

import java.util.Set;

/** Core text shaders whose replacement by a resource pack can break atlas UV assumptions. */
public final class CoreTextShaders {
	private static final Set<Identifier> IDS = Set.of(
		Identifier.withDefaultNamespace("core/text"),
		Identifier.withDefaultNamespace("core/text_background")
	);

	private CoreTextShaders() {
	}

	public static Set<Identifier> ids() {
		return IDS;
	}

	public static Identifier vertexFile(Identifier shaderId) {
		return ShaderType.VERTEX.idConverter().idToFile(shaderId);
	}

	public static Identifier fragmentFile(Identifier shaderId) {
		return ShaderType.FRAGMENT.idConverter().idToFile(shaderId);
	}
}
