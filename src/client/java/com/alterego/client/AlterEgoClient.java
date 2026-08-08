package com.alterego.client;

import com.alterego.net.EgoSyncPayload;
import com.alterego.net.FuseStartPayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class AlterEgoClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		AlterEgoKeybinds.register();

		ClientPlayNetworking.registerGlobalReceiver(EgoSyncPayload.TYPE,
				(payload, context) -> ClientEgoState.set(payload.playerId(), payload.toSelection()));
		ClientPlayNetworking.registerGlobalReceiver(FuseStartPayload.TYPE,
				(payload, context) -> ClientFuseState.start(payload.playerId(), payload.fuseTicks()));

		ClientTickEvents.END_CLIENT_TICK.register(client -> ClientFuseState.tick());

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			ClientEgoState.clearAll();
			ClientFuseState.clear();
			MorphManager.clear();
		});
	}
}
