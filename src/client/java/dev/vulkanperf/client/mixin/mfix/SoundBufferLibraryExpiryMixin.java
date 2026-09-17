package dev.vulkanperf.client.mixin.mfix;

import java.util.Map;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.audio.SoundBuffer;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.Identifier;

/**
 * Decoded sound buffers are kept forever once loaded; servers/clients that touch many sounds
 * accumulate hundreds of megabytes of PCM in native OpenAL buffers. An access-expiry cache
 * frees buffers nothing has used for a while (an active sound keeps its entry hot by lookups).
 */
@Mixin(SoundBufferLibrary.class)
public abstract class SoundBufferLibraryExpiryMixin {
	@Mutable
	@Shadow
	@Final
	private Map<Identifier, java.util.concurrent.CompletableFuture<SoundBuffer>> cache;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void vp$expiryCache(net.minecraft.server.packs.resources.ResourceProvider resourceProvider, CallbackInfo ci) {
		this.cache = CacheBuilder.newBuilder()
			.expireAfterAccess(java.time.Duration.ofSeconds(30))
			// Only entries evicted by the cache itself are freed here. Vanilla's close()
			// discards every buffer explicitly before clearing the map; reacting to its
			// EXPLICIT removals would free the same OpenAL buffer twice.
			.removalListener((RemovalNotification<Identifier, java.util.concurrent.CompletableFuture<SoundBuffer>> notification) -> {
				if (notification.getCause() == RemovalCause.EXPIRED
						|| notification.getCause() == RemovalCause.SIZE) {
					java.util.concurrent.CompletableFuture<SoundBuffer> future = notification.getValue();
					if (future != null) {
						future.thenAccept(SoundBuffer::discardAlBuffer);
					}
				}
			})
			.build()
			.asMap();
	}
}
