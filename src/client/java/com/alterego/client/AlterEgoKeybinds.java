package com.alterego.client;

import com.alterego.AlterEgo;
import com.alterego.client.gui.AlterEgoScreen;
import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

import org.lwjgl.glfw.GLFW;

public final class AlterEgoKeybinds {
	public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(AlterEgo.id("main"));

	public static final KeyMapping OPEN_MENU = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.alterego.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY));

	private AlterEgoKeybinds() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (OPEN_MENU.consumeClick()) {
				if (client.player != null && client.gui.screen() == null) {
					client.gui.setScreen(new AlterEgoScreen());
				}
			}
		});
	}
}
