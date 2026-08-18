package com.alterego.ability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

/**
 * Maps entity types to the abilities a player gains while embodying them.
 * Entity types without an entry simply grant no abilities.
 *
 * Third-party mod entities are keyed by registry name instead of EntityType
 * constants, so mappings are inert when the mod is absent — no hard dependency
 * and no load-order concerns.
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
	private static final Map<Identifier, List<Ability>> MODDED_ABILITIES = new HashMap<>();

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

		// Friends&Foes
		addModded("friendsandfoes:glare", FLIGHT);
		addModded("friendsandfoes:crab", WATER_BREATHING);
		addModded("friendsandfoes:wildfire", SMALL_FIREBALL, FIRE_IMMUNITY);

		// Illager Invasion
		addModded("illagerinvasion:firecaller", SMALL_FIREBALL);
		addModded("illagerinvasion:flying_magma", FLIGHT, FIRE_IMMUNITY);
		addModded("illagerinvasion:surrendered", FLIGHT);

		// Mutant Monsters
		addModded("mutantmonsters:mutant_creeper", EXPLOSION, STRENGTH);
		addModded("mutantmonsters:mutant_enderman", TELEPORT, STRENGTH);
		addModded("mutantmonsters:mutant_skeleton", ARROW_SHOT, STRENGTH);
		addModded("mutantmonsters:mutant_snow_golem", SNOWBALL);
		addModded("mutantmonsters:mutant_zombie", STRENGTH);
		addModded("mutantmonsters:creeper_minion", EXPLOSION);
		addModded("mutantmonsters:endersoul_clone", TELEPORT);
		addModded("mutantmonsters:spider_pig", WALL_CLIMB);

		// Guard Villagers
		addModded("guardvillagers:guard", STRENGTH);

		// Promenade
		addModded("promenade:duck", NO_FALL_DAMAGE);
		addModded("promenade:lush_creeper", EXPLOSION);
		addModded("promenade:sunken", WATER_BREATHING);

		// MCA Reborn
		addModded("mca:grim_reaper", FLIGHT, TELEPORT);

		// BetterEnd
		addModded("betterend:cubozoa", WATER_BREATHING);
		addModded("betterend:end_fish", WATER_BREATHING);
		addModded("betterend:dragonfly", FLIGHT);
		addModded("betterend:silk_moth", FLIGHT);
		addModded("betterend:end_slime", JUMP_BOOST, NO_FALL_DAMAGE);
		addModded("betterend:shadow_walker", SPEED);

		// BetterNether (everything native to the Nether shrugs off fire)
		addModded("betternether:firefly", FLIGHT, FIRE_IMMUNITY);
		addModded("betternether:flying_pig", FLIGHT, FIRE_IMMUNITY);
		addModded("betternether:hydrogen_jellyfish", FLIGHT, FIRE_IMMUNITY);
		addModded("betternether:jungle_skeleton", ARROW_SHOT, FIRE_IMMUNITY);
		addModded("betternether:naga", FIRE_IMMUNITY);
		addModded("betternether:skull", FLIGHT, FIRE_IMMUNITY);

		// Ecologics
		addModded("ecologics:camel", SPEED);
		addModded("ecologics:coconut_crab", WATER_BREATHING, STRENGTH);
		addModded("ecologics:penguin", WATER_BREATHING);
		addModded("ecologics:squirrel", NO_FALL_DAMAGE, SPEED);

		// Fish of Thieves
		addModded("fishofthieves:ancientscale", WATER_BREATHING);
		addModded("fishofthieves:battlegill", WATER_BREATHING);
		addModded("fishofthieves:devilfish", WATER_BREATHING);
		addModded("fishofthieves:islehopper", WATER_BREATHING);
		addModded("fishofthieves:plentifin", WATER_BREATHING);
		addModded("fishofthieves:pondie", WATER_BREATHING);
		addModded("fishofthieves:splashtail", WATER_BREATHING);
		addModded("fishofthieves:stormfish", WATER_BREATHING);
		addModded("fishofthieves:wildsplash", WATER_BREATHING);
		addModded("fishofthieves:wrecker", WATER_BREATHING);

		// Wilder Wild
		addModded("wilderwild:butterfly", FLIGHT);
		addModded("wilderwild:firefly", FLIGHT);
		addModded("wilderwild:jellyfish", WATER_BREATHING);
		addModded("wilderwild:crab", WATER_BREATHING, WALL_CLIMB);
		addModded("wilderwild:penguin", WATER_BREATHING);
		addModded("wilderwild:ostrich", SPEED);
		addModded("wilderwild:zombie_ostrich", SPEED);
		addModded("wilderwild:scorched", WALL_CLIMB, FIRE_IMMUNITY);

		// Variants&Ventures (gelid/verdant are skeleton variants, murk is drowned-like)
		addModded("variantsandventures:gelid", ARROW_SHOT);
		addModded("variantsandventures:verdant", ARROW_SHOT);
		addModded("variantsandventures:murk", WATER_BREATHING);

		// Nycto
		addModded("nycto:vampire", SPEED, STRENGTH);
		addModded("nycto:dark_form", FLIGHT, SPEED);
		addModded("nycto:hunter", ARROW_SHOT);
	}

	private AbilityRegistry() {
	}

	private static void add(EntityType<?> type, Ability... abilities) {
		ABILITIES.put(type, List.of(abilities));
	}

	private static void addModded(String registryName, Ability... abilities) {
		MODDED_ABILITIES.put(Identifier.parse(registryName), List.of(abilities));
	}

	public static List<Ability> abilitiesFor(EntityType<?> type) {
		List<Ability> vanilla = ABILITIES.get(type);
		if (vanilla != null) {
			return vanilla;
		}
		return MODDED_ABILITIES.getOrDefault(EntityType.getKey(type), List.of());
	}
}
