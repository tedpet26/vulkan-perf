package dev.vulkanperf.mixin.logic;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.raid.Raider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Brain.class)
public abstract class BrainMixin {
	@Inject(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)V", at = @At("HEAD"), cancellable = true)
	private void vulkanperf$throttleFarBrain(ServerLevel level, LivingEntity entity, CallbackInfo ci) {
		if (!PerfConfig.get().logic.inactiveAi || entity instanceof ServerPlayer || (entity instanceof Mob mob && mob.isNoAi())) {
			return;
		}
		if (entity instanceof Villager || entity instanceof AbstractPiglin || entity instanceof Raider) {
			return;
		}
		if (level.getNearestPlayer(entity, 32.0) != null) {
			return;
		}
		if ((entity.tickCount & 3) != 0) {
			ci.cancel();
		}
	}
}
