package com.alterego.ability;

import java.util.List;
import java.util.Map;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

/**
 * Maps entity types to the abilities a player gains while embodying them.
 * Entity types without an entry simply grant no abilities.
 */
public final class AbilityRegistry {
	public static final Ability EXPLOSION = new Ability("explosion");

	private static final Map<EntityType<?>, List<Ability>> ABILITIES = Map.of(
			EntityTypes.CREEPER, List.of(EXPLOSION)
	);

	private AbilityRegistry() {
	}

	public static List<Ability> abilitiesFor(EntityType<?> type) {
		return ABILITIES.getOrDefault(type, List.of());
	}
}
