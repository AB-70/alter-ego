package com.alterego.net;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.alterego.AlterEgo;
import com.alterego.EgoSelection;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.EntityType;

/**
 * C2S: the player picked a new ego in the AlterEgo screen (empty type = revert
 * to self).
 */
public record SelectEgoPayload(Optional<EntityType<?>> entityType, boolean showNametag, Set<String> enabledAbilities)
		implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<SelectEgoPayload> TYPE =
			new CustomPacketPayload.Type<>(AlterEgo.id("select_ego"));

	private static final StreamCodec<ByteBuf, Set<String>> ABILITIES_CODEC =
			ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8);

	public static final StreamCodec<RegistryFriendlyByteBuf, SelectEgoPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.optional(ByteBufCodecs.registry(Registries.ENTITY_TYPE)), SelectEgoPayload::entityType,
			ByteBufCodecs.BOOL, SelectEgoPayload::showNametag,
			ABILITIES_CODEC, SelectEgoPayload::enabledAbilities,
			SelectEgoPayload::new);

	public static SelectEgoPayload of(EgoSelection selection) {
		return new SelectEgoPayload(Optional.ofNullable(selection.entityType()),
				selection.showNametag(), selection.enabledAbilities());
	}

	public EgoSelection toSelection() {
		return new EgoSelection(this.entityType.orElse(null), this.showNametag, Set.copyOf(this.enabledAbilities));
	}

	@Override
	public CustomPacketPayload.Type<SelectEgoPayload> type() {
		return TYPE;
	}
}
