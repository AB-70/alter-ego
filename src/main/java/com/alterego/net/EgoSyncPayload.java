package com.alterego.net;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.alterego.AlterEgo;
import com.alterego.EgoSelection;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.EntityType;

/**
 * S2C: broadcast of one player's current ego so every client renders them
 * correctly (empty type = back to their own body).
 */
public record EgoSyncPayload(UUID playerId, Optional<EntityType<?>> entityType, boolean showNametag, Set<String> enabledAbilities)
		implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<EgoSyncPayload> TYPE =
			new CustomPacketPayload.Type<>(AlterEgo.id("ego_sync"));

	private static final StreamCodec<ByteBuf, Set<String>> ABILITIES_CODEC =
			ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8);

	public static final StreamCodec<RegistryFriendlyByteBuf, EgoSyncPayload> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, EgoSyncPayload::playerId,
			ByteBufCodecs.optional(ByteBufCodecs.registry(Registries.ENTITY_TYPE)), EgoSyncPayload::entityType,
			ByteBufCodecs.BOOL, EgoSyncPayload::showNametag,
			ABILITIES_CODEC, EgoSyncPayload::enabledAbilities,
			EgoSyncPayload::new);

	public static EgoSyncPayload of(UUID playerId, EgoSelection selection) {
		return new EgoSyncPayload(playerId, Optional.ofNullable(selection.entityType()),
				selection.showNametag(), selection.enabledAbilities());
	}

	public EgoSelection toSelection() {
		return new EgoSelection(this.entityType.orElse(null), this.showNametag, Set.copyOf(this.enabledAbilities));
	}

	@Override
	public CustomPacketPayload.Type<EgoSyncPayload> type() {
		return TYPE;
	}
}
