package com.alterego.ability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

/**
 * Maps entity types to the abilities a player gains while embodying them.
 * Entity types without an entry simply grant no abilities.
 */
public final class AbilityRegistry {
	// Actives (fired with the ability key, per-player cooldown in ticks).
	public static final Ability EXPLOSION = Ability.active("explosion", 100);
	public static final Ability TELEPORT = Ability.active("teleport", 20);
	public static final Ability SMALL_FIREBALL = Ability.active("small_fireball", 20);
	public static final Ability LARGE_FIREBALL = Ability.active("large_fireball", 40);
	public static final Ability SNOWBALL = Ability.active("snowball", 5);
	public static final Ability SPIT = Ability.active("spit", 10);
	public static final Ability WITHER_SKULL = Ability.active("wither_skull", 30);

	// Passives (continuous while morphed).
	public static final Ability ARROW_SHOT = Ability.passive("arrow_shot");
	public static final Ability WALL_CLIMB = Ability.passive("wall_climb");
	public static final Ability FLIGHT = Ability.passive("flight");
	public static final Ability FIRE_IMMUNITY = Ability.passive("fire_immunity");
	public static final Ability WATER_BREATHING = Ability.passive("water_breathing");
	public static final Ability NO_FALL_DAMAGE = Ability.passive("no_fall_damage");
	public static final Ability JUMP_BOOST = Ability.passive("jump_boost");
	public static final Ability SPEED = Ability.passive("speed");
	public static final Ability STRENGTH = Ability.passive("strength");

	private static final Map<EntityType<?>, List<Ability>> ABILITIES = new HashMap<>();

	static {
		add(EntityTypes.CREEPER, EXPLOSION);
		add(EntityTypes.ENDERMAN, TELEPORT);
		add(EntityTypes.BLAZE, SMALL_FIREBALL, FIRE_IMMUNITY, FLIGHT);
		add(EntityTypes.GHAST, LARGE_FIREBALL, FIRE_IMMUNITY, FLIGHT);
		add(EntityTypes.SKELETON, ARROW_SHOT);
		add(EntityTypes.STRAY, ARROW_SHOT);
		add(EntityTypes.BOGGED, ARROW_SHOT);
		add(EntityTypes.SNOW_GOLEM, SNOWBALL);
		add(EntityTypes.LLAMA, SPIT);
		add(EntityTypes.TRADER_LLAMA, SPIT);
		add(EntityTypes.WITHER, WITHER_SKULL, FIRE_IMMUNITY, FLIGHT);

		add(EntityTypes.SPIDER, WALL_CLIMB);
		add(EntityTypes.CAVE_SPIDER, WALL_CLIMB);

		add(EntityTypes.MAGMA_CUBE, FIRE_IMMUNITY);
		add(EntityTypes.STRIDER, FIRE_IMMUNITY);
		add(EntityTypes.WITHER_SKELETON, FIRE_IMMUNITY);
		add(EntityTypes.ZOMBIFIED_PIGLIN, FIRE_IMMUNITY);

		add(EntityTypes.ALLAY, FLIGHT);
		add(EntityTypes.BAT, FLIGHT);
		add(EntityTypes.BEE, FLIGHT);
		add(EntityTypes.PARROT, FLIGHT);
		add(EntityTypes.VEX, FLIGHT);
		add(EntityTypes.PHANTOM, FLIGHT);
		add(EntityTypes.ENDER_DRAGON, FLIGHT);

		add(EntityTypes.SQUID, WATER_BREATHING);
		add(EntityTypes.GLOW_SQUID, WATER_BREATHING);
		add(EntityTypes.COD, WATER_BREATHING);
		add(EntityTypes.SALMON, WATER_BREATHING);
		add(EntityTypes.TROPICAL_FISH, WATER_BREATHING);
		add(EntityTypes.PUFFERFISH, WATER_BREATHING);
		add(EntityTypes.TADPOLE, WATER_BREATHING);
		add(EntityTypes.GUARDIAN, WATER_BREATHING);
		add(EntityTypes.ELDER_GUARDIAN, WATER_BREATHING);
		add(EntityTypes.AXOLOTL, WATER_BREATHING);
		add(EntityTypes.TURTLE, WATER_BREATHING);
		add(EntityTypes.DROWNED, WATER_BREATHING);
		add(EntityTypes.DOLPHIN, WATER_BREATHING);

		add(EntityTypes.CAT, NO_FALL_DAMAGE, SPEED);
		add(EntityTypes.OCELOT, NO_FALL_DAMAGE, SPEED);
		add(EntityTypes.CHICKEN, NO_FALL_DAMAGE);
		add(EntityTypes.GOAT, NO_FALL_DAMAGE, JUMP_BOOST);
		add(EntityTypes.RABBIT, NO_FALL_DAMAGE, JUMP_BOOST);
		add(EntityTypes.HORSE, SPEED);
		add(EntityTypes.FOX, SPEED);

		add(EntityTypes.IRON_GOLEM, STRENGTH);
		add(EntityTypes.RAVAGER, STRENGTH);
		add(EntityTypes.POLAR_BEAR, STRENGTH);
		add(EntityTypes.WARDEN, STRENGTH);
	}

	private AbilityRegistry() {
	}

	private static void add(EntityType<?> type, Ability... abilities) {
		ABILITIES.put(type, List.of(abilities));
	}

	public static List<Ability> abilitiesFor(EntityType<?> type) {
		return ABILITIES.getOrDefault(type, List.of());
	}
}
