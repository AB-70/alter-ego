package com.alterego;

import java.util.Set;

import net.minecraft.world.entity.EntityType;

import org.jetbrains.annotations.Nullable;

/**
 * A player's chosen alter ego: the entity type to embody (null = original self),
 * whether their nametag stays visible, and which abilities are enabled.
 */
public record EgoSelection(@Nullable EntityType<?> entityType, boolean showNametag, Set<String> enabledAbilities) {
	public static final EgoSelection SELF = new EgoSelection(null, true, Set.of());

	public boolean isSelf() {
		return entityType == null;
	}
}
