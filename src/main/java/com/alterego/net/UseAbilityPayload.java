package com.alterego.net;

import com.alterego.AlterEgo;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** C2S: the player pressed the ability key for one of their ego's abilities. */
public record UseAbilityPayload(String abilityId) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UseAbilityPayload> TYPE =
			new CustomPacketPayload.Type<>(AlterEgo.id("use_ability"));

	public static final StreamCodec<RegistryFriendlyByteBuf, UseAbilityPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, UseAbilityPayload::abilityId,
			UseAbilityPayload::new);

	@Override
	public CustomPacketPayload.Type<UseAbilityPayload> type() {
		return TYPE;
	}
}
