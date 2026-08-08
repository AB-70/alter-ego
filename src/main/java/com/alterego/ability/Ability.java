package com.alterego.ability;

/**
 * A capability granted while embodying a specific entity type. Active
 * abilities fire on the ability key and honor a per-player cooldown; passive
 * abilities apply continuously while the morph is active (immunities,
 * movement, attribute boosts, item grants).
 */
public record Ability(String id, boolean passive, int cooldownTicks) {
	public static Ability active(String id, int cooldownTicks) {
		return new Ability(id, false, cooldownTicks);
	}

	public static Ability passive(String id) {
		return new Ability(id, true, 0);
	}

	public String translationKey() {
		return "ability.alterego." + id;
	}
}
