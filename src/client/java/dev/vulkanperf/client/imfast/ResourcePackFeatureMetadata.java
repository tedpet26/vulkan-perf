package dev.vulkanperf.client.imfast;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.packs.metadata.MetadataSectionType;

import java.util.List;

public record ResourcePackFeatureMetadata(List<String> compatibleFeatures, List<String> incompatibleFeatures) {
	public static final ResourcePackFeatureMetadata DEFAULT = new ResourcePackFeatureMetadata(List.of(), List.of());
	public static final Codec<ResourcePackFeatureMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.listOf().optionalFieldOf("compatible_features", List.of()).forGetter(ResourcePackFeatureMetadata::compatibleFeatures),
		Codec.STRING.listOf().optionalFieldOf("incompatible_features", List.of()).forGetter(ResourcePackFeatureMetadata::incompatibleFeatures)
	).apply(instance, ResourcePackFeatureMetadata::new));
	public static final MetadataSectionType<ResourcePackFeatureMetadata> TYPE = new MetadataSectionType<>("immediatelyfast", CODEC);
}
