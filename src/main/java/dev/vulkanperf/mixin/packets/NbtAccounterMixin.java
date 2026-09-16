package dev.vulkanperf.mixin.packets;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.nbt.NbtAccounter;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NbtAccounter.class)
public abstract class NbtAccounterMixin {
	@Shadow
	@Final
	private long quota;

	@Redirect(method = "accountBytes(J)V", at = @At(value = "FIELD", target = "Lnet/minecraft/nbt/NbtAccounter;quota:J", opcode = Opcodes.GETFIELD, ordinal = 0))
	private long vulkanperf$quota(NbtAccounter instance) {
		return PerfConfig.get().packets.nbtQuota;
	}
}
