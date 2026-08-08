package com.alterego.server;

import com.alterego.AlterEgo;
import com.alterego.EgoSelection;
import com.alterego.ability.AbilityRegistry;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.Holder;

/**
 * Applies and removes the continuous effects of passive abilities on the
 * server player: attribute modifiers, creative-style flight, and per-tick
 * upkeep (fire clearing, air refill). All modifiers are transient (never
 * saved) and tagged with alterego identifiers so removal is exact.
 */
public final class PassiveAbilities {
	private static final Identifier SPEED_ID = AlterEgo.id("morph_speed");
	private static final Identifier JUMP_ID = AlterEgo.id("morph_jump");
	private static final Identifier STRENGTH_ID = AlterEgo.id("morph_strength");
	private static final Identifier NO_FALL_ID = AlterEgo.id("morph_no_fall");

	private PassiveAbilities() {
	}

	/** Removes all morph passives, then applies the ones the selection enables. */
	public static void refresh(ServerPlayer player, EgoSelection selection) {
		removeAll(player);
		if (selection.isSelf()) {
			return;
		}
		if (enabled(selection, AbilityRegistry.SPEED)) {
			addModifier(player, Attributes.MOVEMENT_SPEED,
					new AttributeModifier(SPEED_ID, 0.4, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
		}
		if (enabled(selection, AbilityRegistry.JUMP_BOOST)) {
			addModifier(player, Attributes.JUMP_STRENGTH,
					new AttributeModifier(JUMP_ID, 0.4, AttributeModifier.Operation.ADD_VALUE));
		}
		if (enabled(selection, AbilityRegistry.STRENGTH)) {
			addModifier(player, Attributes.ATTACK_DAMAGE,
					new AttributeModifier(STRENGTH_ID, 8.0, AttributeModifier.Operation.ADD_VALUE));
		}
		if (enabled(selection, AbilityRegistry.NO_FALL_DAMAGE)) {
			addModifier(player, Attributes.FALL_DAMAGE_MULTIPLIER,
					new AttributeModifier(NO_FALL_ID, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		}
		if (enabled(selection, AbilityRegistry.FLIGHT)) {
			player.getAbilities().mayfly = true;
			player.onUpdateAbilities();
		}
		if (enabled(selection, AbilityRegistry.ARROW_SHOT)) {
			MorphItems.grantBow(player);
		}
	}

	public static void removeAll(ServerPlayer player) {
		removeModifier(player, Attributes.MOVEMENT_SPEED, SPEED_ID);
		removeModifier(player, Attributes.JUMP_STRENGTH, JUMP_ID);
		removeModifier(player, Attributes.ATTACK_DAMAGE, STRENGTH_ID);
		removeModifier(player, Attributes.FALL_DAMAGE_MULTIPLIER, NO_FALL_ID);
		if (!player.isCreative() && !player.isSpectator()) {
			player.getAbilities().mayfly = false;
			player.getAbilities().flying = false;
		}
		player.onUpdateAbilities();
		MorphItems.reclaim(player);
	}

	/** Per-tick upkeep for the player's enabled passives. */
	public static void tick(ServerPlayer player, EgoSelection selection) {
		if (enabled(selection, AbilityRegistry.FIRE_IMMUNITY)) {
			player.clearFire();
		}
		if (enabled(selection, AbilityRegistry.WATER_BREATHING)) {
			player.setAirSupply(player.getMaxAirSupply());
		}
	}

	/** Damage gate for immunity passives; false cancels the damage. */
	public static boolean allowDamage(EgoSelection selection, DamageSource source) {
		return !(enabled(selection, AbilityRegistry.FIRE_IMMUNITY) && source.is(DamageTypeTags.IS_FIRE));
	}

	private static boolean enabled(EgoSelection selection, com.alterego.ability.Ability ability) {
		return selection.enabledAbilities().contains(ability.id());
	}

	private static void addModifier(ServerPlayer player, Holder<Attribute> attribute, AttributeModifier modifier) {
		AttributeInstance instance = player.getAttribute(attribute);
		if (instance != null) {
			instance.addOrUpdateTransientModifier(modifier);
		}
	}

	private static void removeModifier(ServerPlayer player, Holder<Attribute> attribute, Identifier id) {
		AttributeInstance instance = player.getAttribute(attribute);
		if (instance != null) {
			instance.removeModifier(id);
		}
	}
}
