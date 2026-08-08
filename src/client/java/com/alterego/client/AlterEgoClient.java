package com.alterego.client;

import net.fabricmc.api.ClientModInitializer;

public class AlterEgoClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		AlterEgoKeybinds.register();
	}
}
