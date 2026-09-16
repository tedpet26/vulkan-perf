package dev.vulkanperf;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.vulkanperf.chunks.ChunkWorkers;
import dev.vulkanperf.config.PerfConfig;
import dev.vulkanperf.logging.LogSpamFilter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VulkanPerf implements ModInitializer {
	public static final String MOD_ID = "vulkanperf";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		PerfConfig.load();
		if (PerfConfig.get().logging.enabled) {
			LogSpamFilter.install();
		}
		if (PerfConfig.get().chunks.enabled) {
			ChunkWorkers.start();
		}
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
			Commands.literal("vulkanperf")
				.then(Commands.literal("reload").executes(ctx -> {
					PerfConfig.load();
					ctx.getSource().sendSuccess(() -> Component.literal("vulkan-perf config reloaded"), false);
					return 1;
				}))
				.then(Commands.literal("save").executes(ctx -> {
					PerfConfig.save();
					ctx.getSource().sendSuccess(() -> Component.literal("vulkan-perf config saved"), false);
					return 1;
				}))
				.then(Commands.literal("module")
					.then(Commands.argument("name", StringArgumentType.word())
						.then(Commands.argument("enabled", BoolArgumentType.bool())
							.executes(ctx -> {
								String name = StringArgumentType.getString(ctx, "name");
								boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
								if (!setModule(name, enabled)) {
									ctx.getSource().sendFailure(Component.literal("Unknown module: " + name));
									return 0;
								}
								PerfConfig.save();
								ctx.getSource().sendSuccess(() -> Component.literal(name + " = " + enabled + " (restart if mixin-gated)"), false);
								return 1;
							}))))
		));
		LOGGER.info("vulkan-perf initialized");
	}

	private static boolean setModule(String name, boolean enabled) {
		PerfConfig c = PerfConfig.get();
		return switch (name) {
			case "logic" -> { c.logic.enabled = enabled; yield true; }
			case "chunks" -> { c.chunks.enabled = enabled; yield true; }
			case "packets" -> { c.packets.enabled = enabled; yield true; }
			case "net" -> { c.net.enabled = enabled; yield true; }
			case "memory" -> { c.memory.enabled = enabled; yield true; }
			case "logging" -> { c.logging.enabled = enabled; yield true; }
			case "power" -> { c.power.enabled = enabled; yield true; }
			case "particles" -> { c.particles.enabled = enabled; yield true; }
			case "culling" -> { c.culling.enabled = enabled; yield true; }
			case "clientcache" -> { c.clientcache.enabled = enabled; yield true; }
			case "hudspread" -> { c.hudspread.enabled = enabled; yield true; }
			case "batching" -> { c.batching.enabled = enabled; yield true; }
			case "reloadui" -> { c.reloadui.enabled = enabled; yield true; }
			case "ping" -> { c.ping.enabled = enabled; yield true; }
			case "extras" -> { c.extras.enabled = enabled; yield true; }
			case "window" -> { c.window.enabled = enabled; yield true; }
			case "input" -> { c.input.enabled = enabled; yield true; }
			case "blockentities" -> { c.blockentities.enabled = enabled; yield true; }
			case "vanillafixes" -> { c.vanillafixes.enabled = enabled; yield true; }
			default -> false;
		};
	}
}
