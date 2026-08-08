package com.alterego.ability;

/**
 * A toggleable capability granted while embodying a specific entity type,
 * e.g. a creeper's explosion. Identified by a stable string id used for
 * config, networking and translation keys.
 */
public record Ability(String id) {
	public String translationKey() {
		return "ability.alterego." + id;
	}
}
