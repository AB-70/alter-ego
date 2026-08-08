package com.alterego;

import com.alterego.net.EgoSyncPayload;
import com.alterego.net.FuseStartPayload;
import com.alterego.net.SelectEgoPayload;
import com.alterego.net.UseAbilityPayload;
import com.alterego.server.EgoManager;
import com.alterego.server.PassiveAbilities;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlterEgo implements ModInitializer {
	public static final String MOD_ID = "alterego";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.serverboundPlay().register(SelectEgoPayload.TYPE, SelectEgoPayload.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(UseAbilityPayload.TYPE, UseAbilityPayload.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(EgoSyncPayload.TYPE, EgoSyncPayload.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(FuseStartPayload.TYPE, FuseStartPayload.STREAM_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(SelectEgoPayload.TYPE,
				(payload, context) -> EgoManager.select(context.player(), payload.toSelection()));
		ServerPlayNetworking.registerGlobalReceiver(UseAbilityPayload.TYPE,
				(payload, context) -> EgoManager.useAbility(context.player(), payload.abilityId()));

		ServerTickEvents.END_SERVER_TICK.register(EgoManager::tick);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> EgoManager.onJoin(handler.player));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> EgoManager.onDisconnect(handler.player));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> EgoManager.clearAll());

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> EgoManager.onRespawn(newPlayer));
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (entity instanceof ServerPlayer player) {
				EgoSelection ego = EgoManager.get(player.getUUID());
				if (ego != null) {
					return PassiveAbilities.allowDamage(ego, source);
				}
			}
			return true;
		});

		LOGGER.info("AlterEgo initialized");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
