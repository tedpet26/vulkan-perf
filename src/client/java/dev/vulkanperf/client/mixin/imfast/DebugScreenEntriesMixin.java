package dev.vulkanperf.client.mixin.imfast;

import dev.vulkanperf.client.imfast.ImFastDebugEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(DebugScreenEntries.class)
public abstract class DebugScreenEntriesMixin {
	@Shadow
	@Final
	@Mutable
	public static Map<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> PROFILES;

	@Inject(method = "<clinit>", at = @At("RETURN"))
	private static void vulkanperf$imfastDebugEntry(CallbackInfo ci) {
		Identifier id = DebugScreenEntries.register(ImFastDebugEntry.ID, new ImFastDebugEntry());
		Map<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> profiles = new HashMap<>();
		for (Map.Entry<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> entry : PROFILES.entrySet()) {
			Map<Identifier, DebugScreenEntryStatus> values = new HashMap<>(entry.getValue());
			values.put(id, DebugScreenEntryStatus.IN_OVERLAY);
			profiles.put(entry.getKey(), values);
		}
		PROFILES = Map.copyOf(profiles);
	}
}
