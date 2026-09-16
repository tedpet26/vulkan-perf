package dev.vulkanperf.client.extras;

import dev.vulkanperf.config.PerfConfig;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.StatefulOptionBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ExtrasSodiumConfig implements ConfigEntryPoint, StorageEventHandler {
	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("vulkanperf", path);
	}

	private StatefulOptionBuilder<?> flag(ConfigBuilder builder, String key, String name, String tooltip, Consumer<Boolean> setter, Supplier<Boolean> getter, boolean def) {
		return builder.createBooleanOption(id(key))
			.setName(Component.literal(name))
			.setTooltip(Component.literal(tooltip))
			.setStorageHandler(this)
			.setBinding(setter, getter)
			.setDefaultValue(def);
	}

	@Override
	public void registerConfigLate(ConfigBuilder builder) {
		PerfConfig.ExtrasConfig extras = PerfConfig.get().extras;
		builder.registerOwnModOptions()
			.setIcon(Identifier.fromNamespaceAndPath("minecraft", "textures/item/redstone.png"))
			.addPage(builder.createOptionPage()
				.setName(Component.literal("Animation"))
				.addOptionGroup(builder.createOptionGroup()
					.addOption(flag(builder, "animations_all", "Animations", "Master toggle for animated textures", v -> extras.animation = v, () -> extras.animation, true))
					.addOption(flag(builder, "animate_water", "Water", "Animated water textures", v -> extras.animateWater = v, () -> extras.animateWater, true))
					.addOption(flag(builder, "animate_lava", "Lava", "Animated lava textures", v -> extras.animateLava = v, () -> extras.animateLava, true))
					.addOption(flag(builder, "animate_fire", "Fire", "Animated fire textures", v -> extras.animateFire = v, () -> extras.animateFire, true))
					.addOption(flag(builder, "animate_portal", "Portal", "Animated portal textures", v -> extras.animatePortal = v, () -> extras.animatePortal, true))
					.addOption(flag(builder, "block_animations", "Block animations", "Other animated block textures", v -> extras.animateBlocks = v, () -> extras.animateBlocks, true))
					.addOption(flag(builder, "animate_sculk", "Sculk", "Animated sculk textures", v -> extras.animateSculk = v, () -> extras.animateSculk, true))))
			.addPage(builder.createOptionPage()
				.setName(Component.literal("Particles"))
				.addOptionGroup(builder.createOptionGroup()
					.addOption(flag(builder, "particles_all", "Particles", "Master toggle for particles", v -> extras.particles = v, () -> extras.particles, true))
					.addOption(flag(builder, "weather_particles", "Weather particles", "Rain splash and weather particles", v -> extras.weatherParticles = v, () -> extras.weatherParticles, true))
					.addOption(flag(builder, "rain_splash", "Rain splash", "Rain splash particles on the ground", v -> extras.rainSplash = v, () -> extras.rainSplash, true))
					.addOption(flag(builder, "block_break", "Block break", "Particles when a block is broken", v -> extras.blockBreak = v, () -> extras.blockBreak, true))
					.addOption(flag(builder, "block_breaking", "Block cracking", "Particles while mining a block", v -> extras.blockBreaking = v, () -> extras.blockBreaking, true))))
			.addPage(builder.createOptionPage()
				.setName(Component.literal("Detail"))
				.addOptionGroup(builder.createOptionGroup()
					.addOption(flag(builder, "sky", "Sky", "Render the sky disc", v -> extras.sky = v, () -> extras.sky, true))
					.addOption(flag(builder, "sun", "Sun", "Render the sun", v -> extras.sun = v, () -> extras.sun, true))
					.addOption(flag(builder, "moon", "Moon", "Render the moon", v -> extras.moon = v, () -> extras.moon, true))
					.addOption(flag(builder, "stars", "Stars", "Render stars", v -> extras.stars = v, () -> extras.stars, true))
					.addOption(flag(builder, "rain_snow", "Rain & snow", "Render rain and snow weather", v -> extras.rainSnow = v, () -> extras.rainSnow, true))
					.addOption(flag(builder, "biome_colors", "Biome colors", "Use biome-tinted grass and foliage", v -> extras.biomeColors = v, () -> extras.biomeColors, true))
					.addOption(flag(builder, "sky_colors", "Sky colors", "Use biome sky colors", v -> extras.skyColors = v, () -> extras.skyColors, true))))
			.addPage(builder.createOptionPage()
				.setName(Component.literal("Render"))
				.addOptionGroup(builder.createOptionGroup()
					.addOption(flag(builder, "fog", "Fog", "Terrain fog and Sodium fog culling", v -> extras.fog = v, () -> extras.fog, true))
					.addOption(flag(builder, "light_updates", "Light updates", "Process lighting engine updates", v -> extras.lightUpdates = v, () -> extras.lightUpdates, true))
					.addOption(flag(builder, "item_frames", "Item frames", "Render item frames", v -> extras.itemFrames = v, () -> extras.itemFrames, true))
					.addOption(flag(builder, "armor_stands", "Armor stands", "Render armor stands", v -> extras.armorStands = v, () -> extras.armorStands, true))
					.addOption(flag(builder, "paintings", "Paintings", "Render paintings", v -> extras.paintings = v, () -> extras.paintings, true))
					.addOption(flag(builder, "pistons", "Piston animations", "Render moving pistons", v -> extras.pistons = v, () -> extras.pistons, true))
					.addOption(flag(builder, "beacon_beams", "Beacon beams", "Render beacon beams", v -> extras.beaconBeams = v, () -> extras.beaconBeams, true))
					.addOption(flag(builder, "enchanting_table_book", "Enchanting table book", "Render the enchanting table book", v -> extras.enchantingTableBook = v, () -> extras.enchantingTableBook, true))
					.addOption(flag(builder, "nametags", "Player nametags", "Render player nametags", v -> extras.nametags = v, () -> extras.nametags, true))
					.addOption(flag(builder, "item_frame_nametags", "Item frame nametags", "Render item frame nametags", v -> extras.itemFrameNametags = v, () -> extras.itemFrameNametags, true))))
			.addPage(builder.createOptionPage()
				.setName(Component.literal("Extra"))
				.addOptionGroup(builder.createOptionGroup()
					.addOption(flag(builder, "show_fps", "FPS overlay", "Show FPS in the corner of the HUD", v -> extras.overlayFps = v, () -> extras.overlayFps, true))
					.addOption(flag(builder, "show_fps_extended", "FPS overlay extended", "Show average and low FPS", v -> extras.overlayFpsExtended = v, () -> extras.overlayFpsExtended, false))
					.addOption(flag(builder, "show_coordinates", "Coordinate overlay", "Show player coordinates on the HUD", v -> extras.overlayCoords = v, () -> extras.overlayCoords, false))
					.addOption(flag(builder, "toasts", "Toasts", "Show advancement and recipe toasts", v -> extras.toasts = v, () -> extras.toasts, true))));
	}

	@Override
	public void afterSave() {
		PerfConfig.save();
	}
}
