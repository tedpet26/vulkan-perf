package dev.vulkanperf.mixin.memory.thread;

import dev.vulkanperf.memory.thread.CompactThreadGuard;
import dev.vulkanperf.memory.thread.ThreadGuardState;
import net.minecraft.util.ThreadingDetector;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Opt-in (default off) replacement of {@code PalettedContainer}'s full
 * {@code ThreadingDetector} field with a single byte of guard state. Every
 * loaded chunk section holds several of these containers, so on large
 * servers the per-object overhead of the full detector adds up.
 */
@Mixin(PalettedContainer.class)
public abstract class PalettedContainerThreadMixin implements ThreadGuardState {

	@Shadow
	@Final
	@Mutable
	private ThreadingDetector threadingDetector;

	@Unique
	private byte vulkanperf$guardState = ThreadGuardState.UNLOCKED;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void vulkanperf$dropVanillaDetector(CallbackInfo ci) {
		// Field initializer always constructs a full ThreadingDetector; drop it in every
		// constructor (public (value, strategy), copy, and packed-data unpack) so the
		// compact byte guard is the only retained state.
		this.threadingDetector = null;
	}

	/**
	 * @author vulkanperf
	 * @reason The vanilla detector field is nulled out by this mixin; guard
	 * acquisition is instead tracked with a single byte field.
	 */
	@Overwrite
	public void acquire() {
		CompactThreadGuard.acquire(this, "PalettedContainer");
	}

	/**
	 * @author vulkanperf
	 * @reason See {@link #acquire()}.
	 */
	@Overwrite
	public void release() {
		CompactThreadGuard.release(this);
	}

	@Override
	public byte vulkanperf$getGuardState() {
		return this.vulkanperf$guardState;
	}

	@Override
	public void vulkanperf$setGuardState(byte state) {
		this.vulkanperf$guardState = state;
	}
}
