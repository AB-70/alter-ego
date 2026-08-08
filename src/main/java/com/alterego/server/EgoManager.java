package com.alterego.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.alterego.EgoSelection;
import com.alterego.ability.Ability;
import com.alterego.ability.AbilityRegistry;
import com.alterego.net.EgoSyncPayload;
import com.alterego.net.FuseStartPayload;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

/**
 * Server-authoritative ego state: which entity each player currently embodies,
 * plus ability execution (creeper fuse + explosion). All mutation happens on
 * the server thread via network handlers and tick events.
 */
public final class EgoManager {
	private static final int FUSE_TICKS = 30;
	private static final float EXPLOSION_POWER = 3.0F;

	private static final Map<UUID, EgoSelection> EGOS = new HashMap<>();
	/** Players currently "hissing": ticks left until their explosion goes off. */
	private static final Map<UUID, Integer> FUSES = new HashMap<>();
	/** Per-player, per-ability cooldown ticks remaining. */
	private static final Map<UUID, Map<String, Integer>> COOLDOWNS = new HashMap<>();

	private EgoManager() {
	}

	/** The player's current ego, or null if they are themself. */
	public static EgoSelection get(UUID playerId) {
		return EGOS.get(playerId);
	}

	/** Re-applies passives on a fresh ServerPlayer instance (respawn). */
	public static void onRespawn(ServerPlayer player) {
		EgoSelection ego = EGOS.get(player.getUUID());
		if (ego != null) {
			PassiveAbilities.refresh(player, ego);
		}
	}

	public static void select(ServerPlayer player, EgoSelection requested) {
		MinecraftServer server = player.level().getServer();
		EgoSelection selection = sanitize(requested);
		if (selection.isSelf()) {
			EGOS.remove(player.getUUID());
		} else {
			EGOS.put(player.getUUID(), selection);
		}
		PassiveAbilities.refresh(player, selection);
		broadcast(server, EgoSyncPayload.of(player.getUUID(), selection));
	}

	/** Keeps only abilities the chosen entity type actually grants. */
	private static EgoSelection sanitize(EgoSelection requested) {
		if (requested.isSelf()) {
			return EgoSelection.SELF;
		}
		Set<String> enabled = new HashSet<>();
		for (Ability ability : AbilityRegistry.abilitiesFor(requested.entityType())) {
			if (requested.enabledAbilities().contains(ability.id())) {
				enabled.add(ability.id());
			}
		}
		return new EgoSelection(requested.entityType(), requested.showNametag(), Set.copyOf(enabled));
	}

	public static void useAbility(ServerPlayer player, String abilityId) {
		EgoSelection ego = EGOS.get(player.getUUID());
		if (ego == null || !ego.enabledAbilities().contains(abilityId)) {
			return;
		}
		Ability ability = null;
		for (Ability candidate : AbilityRegistry.abilitiesFor(ego.entityType())) {
			if (!candidate.passive() && candidate.id().equals(abilityId)) {
				ability = candidate;
				break;
			}
		}
		if (ability == null) {
			return;
		}

		UUID id = player.getUUID();
		Map<String, Integer> cooldowns = COOLDOWNS.computeIfAbsent(id, k -> new HashMap<>());
		if (cooldowns.containsKey(abilityId)) {
			return;
		}

		if (ability == AbilityRegistry.EXPLOSION) {
			if (FUSES.containsKey(id)) {
				return;
			}
			player.level().playSound(null, player, SoundEvents.CREEPER_PRIMED, SoundSource.HOSTILE, 1.0F, 0.5F);
			FUSES.put(id, FUSE_TICKS);
			broadcast(player.level().getServer(), new FuseStartPayload(id, FUSE_TICKS));
		} else if (ActiveAbilities.use(player, ability)) {
			cooldowns.put(abilityId, ability.cooldownTicks());
		}
	}

	public static void tick(MinecraftServer server) {
		for (Map<String, Integer> cooldowns : COOLDOWNS.values()) {
			cooldowns.replaceAll((abilityId, ticks) -> ticks - 1);
			cooldowns.values().removeIf(ticks -> ticks <= 0);
		}
		COOLDOWNS.values().removeIf(Map::isEmpty);

		Iterator<Map.Entry<UUID, Integer>> it = FUSES.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<UUID, Integer> entry = it.next();
			int remaining = entry.getValue() - 1;
			if (remaining > 0) {
				entry.setValue(remaining);
				continue;
			}
			it.remove();
			ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
			if (player != null && !player.isRemoved()) {
				player.level().explode(player, player.getX(), player.getY(), player.getZ(),
						EXPLOSION_POWER, Level.ExplosionInteraction.MOB);
				COOLDOWNS.computeIfAbsent(entry.getKey(), k -> new HashMap<>())
						.put(AbilityRegistry.EXPLOSION.id(), AbilityRegistry.EXPLOSION.cooldownTicks());
			}
		}

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			EgoSelection ego = EGOS.get(player.getUUID());
			if (ego != null) {
				PassiveAbilities.tick(player, ego);
			}
		}
	}

	/** Late joiners need to learn about everyone's current ego. */
	public static void onJoin(ServerPlayer player) {
		for (Map.Entry<UUID, EgoSelection> entry : EGOS.entrySet()) {
			ServerPlayNetworking.send(player, EgoSyncPayload.of(entry.getKey(), entry.getValue()));
		}
	}

	public static void onDisconnect(ServerPlayer player) {
		UUID id = player.getUUID();
		FUSES.remove(id);
		COOLDOWNS.remove(id);
		if (EGOS.remove(id) != null) {
			PassiveAbilities.removeAll(player);
			broadcast(player.level().getServer(), EgoSyncPayload.of(id, EgoSelection.SELF));
		}
	}

	public static void clearAll() {
		EGOS.clear();
		FUSES.clear();
		COOLDOWNS.clear();
	}

	private static void broadcast(MinecraftServer server, CustomPacketPayload payload) {
		for (ServerPlayer player : PlayerLookup.all(server)) {
			ServerPlayNetworking.send(player, payload);
		}
	}
}
