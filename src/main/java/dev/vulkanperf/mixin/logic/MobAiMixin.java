package dev.vulkanperf.mixin.logic;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Throttles expensive goal/nav AI for far non-hostile mobs when no player is nearby.
 */
@Mixin(Mob.class)
public abstract class MobAiMixin {
	@Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$throttleFarAi(CallbackInfo ci) {
		if (!PerfConfig.get().logic.mobAiSkip) {
			return;
		}
		Mob self = (Mob) (Object) this;
		if (!(self.level() instanceof ServerLevel level) || self.isNoAi()) {
			return;
		}
		if (self instanceof Enemy || self instanceof Villager) {
			return;
		}
		if (level.getNearestPlayer(self, 48.0) != null) {
			return;
		}
		// Non-hostile mobs: run full AI every 4th tick when far from players.
		if ((self.tickCount + self.getId()) % 4 != 0) {
			ci.cancel();
		}
	}
}
