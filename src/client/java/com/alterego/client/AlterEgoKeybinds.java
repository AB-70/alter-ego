package com.alterego.client;

import com.alterego.AlterEgo;
import com.alterego.EgoSelection;
import com.alterego.ability.Ability;
import com.alterego.ability.AbilityRegistry;
import com.alterego.client.gui.AlterEgoScreen;
import com.alterego.net.UseAbilityPayload;
import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import org.lwjgl.glfw.GLFW;

public final class AlterEgoKeybinds {
	public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(AlterEgo.id("main"));

	public static final KeyMapping OPEN_MENU = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.alterego.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY));

	public static final KeyMapping USE_ABILITY = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.alterego.ability", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY));

	private AlterEgoKeybinds() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (OPEN_MENU.consumeClick()) {
				if (client.player != null && client.gui.screen() == null) {
					client.gui.setScreen(new AlterEgoScreen());
				}
			}
			while (USE_ABILITY.consumeClick()) {
				useFirstEnabledAbility(client);
			}
		});
	}

	private static void useFirstEnabledAbility(Minecraft client) {
		if (client.player == null) {
			return;
		}
		EgoSelection ego = ClientEgoState.get(client.player.getUUID());
		if (ego.isSelf()) {
			return;
		}
		for (Ability ability : AbilityRegistry.abilitiesFor(ego.entityType())) {
			if (!ability.passive() && ego.enabledAbilities().contains(ability.id())) {
				ClientPlayNetworking.send(new UseAbilityPayload(ability.id()));
				return;
			}
		}
	}
}
