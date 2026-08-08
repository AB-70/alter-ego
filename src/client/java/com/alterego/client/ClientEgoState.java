package com.alterego.client;

import com.alterego.EgoSelection;

/**
 * Client-side holder of the local player's current ego selection.
 * Once server sync exists, this becomes a mirror of the server-authoritative
 * state; for now Apply writes here directly.
 */
public final class ClientEgoState {
	private static EgoSelection current = EgoSelection.SELF;

	private ClientEgoState() {
	}

	public static EgoSelection current() {
		return current;
	}

	public static void set(EgoSelection selection) {
		current = selection;
	}
}
