package dev.vulkanperf.mixin;

import dev.vulkanperf.config.PerfConfig;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class VulkanPerfMixinPlugin implements IMixinConfigPlugin {
	@Override
	public void onLoad(String mixinPackage) {
		PerfConfig.load();
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		PerfConfig config = PerfConfig.get();
		String name = mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1);
		if (mixinClassName.contains(".logic.")) {
			if (!config.logic.enabled) {
				return false;
			}
			return switch (name) {
				case "LevelCollisionMixin" -> config.logic.collisionCache;
				case "HopperBlockEntityMixin" -> config.logic.hopper;
				case "BrainMixin" -> config.logic.inactiveAi;
				case "ShapesJoinMixin" -> config.logic.voxelShapes;
				case "PathNavigationMixin" -> config.logic.pathCache;
				default -> true;
			};
		}
		if (mixinClassName.contains(".chunks.")) {
			return config.chunks.enabled;
		}
		if (mixinClassName.contains(".packets.")) {
			return config.packets.enabled;
		}
		if (mixinClassName.contains(".logging.")) {
			return config.logging.enabled;
		}
		if (mixinClassName.contains(".power.")) {
			return config.power.enabled;
		}
		if (mixinClassName.contains(".particles.")) {
			return config.particles.enabled;
		}
		if (mixinClassName.contains(".culling.")) {
			return config.culling.enabled;
		}
		if (mixinClassName.contains(".clientcache.")) {
			return config.clientcache.enabled;
		}
		if (mixinClassName.contains(".hudspread.")) {
			return config.hudspread.enabled;
		}
		if (mixinClassName.contains(".batching.")) {
			return config.batching.enabled;
		}
		if (mixinClassName.contains(".reloadui.")) {
			return config.reloadui.enabled;
		}
		if (mixinClassName.contains(".ping.")) {
			return config.ping.enabled;
		}
		if (mixinClassName.contains(".extras.")) {
			return config.extras.enabled;
		}
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
}
