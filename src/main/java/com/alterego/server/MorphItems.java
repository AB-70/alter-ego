package com.alterego.server;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Items granted by morphs (skeleton's Infinity bow + arrow). Granted stacks
 * carry a custom-data marker so they can be reclaimed exactly on revert,
 * respawn or disconnect, wherever they sit in the inventory.
 */
public final class MorphItems {
	private static final String MARKER = "alterego_morph_item";
	private static final CompoundTag MARKER_TAG = new CompoundTag();

	static {
		MARKER_TAG.putBoolean(MARKER, true);
	}

	private MorphItems() {
	}

	public static void grantBow(ServerPlayer player) {
		if (hasMarkedItem(player)) {
			return;
		}
		ItemStack bow = new ItemStack(Items.BOW);
		Holder<Enchantment> infinity = player.level().registryAccess()
				.lookupOrThrow(Registries.ENCHANTMENT)
				.getOrThrow(Enchantments.INFINITY);
		bow.enchant(infinity, 1);
		CustomData.update(DataComponents.CUSTOM_DATA, bow, tag -> tag.putBoolean(MARKER, true));

		// Infinity still needs one arrow present to fire.
		ItemStack arrow = new ItemStack(Items.ARROW);
		CustomData.update(DataComponents.CUSTOM_DATA, arrow, tag -> tag.putBoolean(MARKER, true));

		give(player, bow);
		give(player, arrow);
	}

	public static void reclaim(ServerPlayer player) {
		Inventory inventory = player.getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (isMarked(inventory.getItem(slot))) {
				inventory.setItem(slot, ItemStack.EMPTY);
			}
		}
	}

	private static boolean hasMarkedItem(ServerPlayer player) {
		Inventory inventory = player.getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (isMarked(inventory.getItem(slot))) {
				return true;
			}
		}
		return false;
	}

	private static boolean isMarked(ItemStack stack) {
		return !stack.isEmpty()
				&& stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).matchedBy(MARKER_TAG);
	}

	private static void give(ServerPlayer player, ItemStack stack) {
		if (!player.getInventory().add(stack)) {
			player.drop(stack, false);
		}
	}
}
