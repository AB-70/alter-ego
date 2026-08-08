package com.alterego.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Client-side fuse timers for the creeper explosion visual: while a player's
 * fuse runs, their morph's swell value ramps 0 → 1 so the vanilla creeper
 * renderer produces the swell + white flash.
 */
public final class ClientFuseState {
	private static final class Fuse {
		final int total;
		int remaining;

		Fuse(int total) {
			this.total = total;
			this.remaining = total;
		}
	}

	private static final Map<UUID, Fuse> FUSES = new HashMap<>();

	private ClientFuseState() {
	}

	public static void start(UUID playerId, int fuseTicks) {
		FUSES.put(playerId, new Fuse(fuseTicks));
	}

	/** Called once per client tick. */
	public static void tick() {
		FUSES.values().removeIf(fuse -> --fuse.remaining <= 0);
	}

	/** Swell progress 0..1, or -1 if this player has no active fuse. */
	public static float progress(UUID playerId, float partialTick) {
		Fuse fuse = FUSES.get(playerId);
		if (fuse == null) {
			return -1.0F;
		}
		return Math.min(1.0F, (fuse.total - fuse.remaining + partialTick) / fuse.total);
	}

	public static void clear() {
		FUSES.clear();
	}
}
