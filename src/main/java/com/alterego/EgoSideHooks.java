package com.alterego;

import java.util.UUID;
import java.util.function.Function;

/**
 * Side-agnostic ego lookup for code shared by both logical sides (mixins).
 * The client entrypoint swaps in a lookup backed by its synced state; the
 * server side queries EgoManager directly. Split source sets prevent the
 * common mixin from referencing client classes, hence this indirection.
 */
public final class EgoSideHooks {
	public static volatile Function<UUID, EgoSelection> clientEgoLookup = id -> EgoSelection.SELF;

	private EgoSideHooks() {
	}
}
