package com.alterego.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.alterego.EgoSelection;

/**
 * Client-side mirror of every player's server-authoritative ego selection,
 * keyed by player UUID. Populated by sync packets; read by rendering and the
 * picker screen.
 */
public final class ClientEgoState {
	private static final Map<UUID, EgoSelection> EGOS = new HashMap<>();

	private ClientEgoState() {
	}

	public static EgoSelection get(UUID playerId) {
		return EGOS.getOrDefault(playerId, EgoSelection.SELF);
	}

	public static void set(UUID playerId, EgoSelection selection) {
		if (selection.isSelf()) {
			EGOS.remove(playerId);
		} else {
			EGOS.put(playerId, selection);
		}
	}

	public static void clearAll() {
		EGOS.clear();
	}
}
