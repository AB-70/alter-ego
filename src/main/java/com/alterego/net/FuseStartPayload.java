package com.alterego.net;

import java.util.UUID;

import com.alterego.AlterEgo;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * S2C: a morphed player triggered their explosion fuse; clients animate the
 * creeper swell/flash on that player for the given number of ticks.
 */
public record FuseStartPayload(UUID playerId, int fuseTicks) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<FuseStartPayload> TYPE =
			new CustomPacketPayload.Type<>(AlterEgo.id("fuse_start"));

	public static final StreamCodec<RegistryFriendlyByteBuf, FuseStartPayload> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, FuseStartPayload::playerId,
			ByteBufCodecs.VAR_INT, FuseStartPayload::fuseTicks,
			FuseStartPayload::new);

	@Override
	public CustomPacketPayload.Type<FuseStartPayload> type() {
		return TYPE;
	}
}
