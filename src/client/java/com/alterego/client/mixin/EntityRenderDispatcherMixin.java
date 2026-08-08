package com.alterego.client.mixin;

import com.alterego.EgoSelection;
import com.alterego.client.ClientEgoState;
import com.alterego.client.ClientFuseState;
import com.alterego.client.MorphManager;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The single choke point every world entity passes through on its way to the
 * renderer. For players with an active ego we extract the morph dummy's render
 * state instead, so the chosen entity's renderer takes over entirely — in
 * third person and for all other players. The re-entrant extractEntity call is
 * safe: dummies are never AbstractClientPlayer instances.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
	@Inject(method = "extractEntity", at = @At("HEAD"), cancellable = true)
	private <E extends Entity> void alterego$swapEgo(E entity, float partialTicks,
			CallbackInfoReturnable<EntityRenderState> cir) {
		if (!(entity instanceof AbstractClientPlayer player)) {
			return;
		}
		EgoSelection ego = ClientEgoState.get(player.getUUID());
		if (ego.isSelf()) {
			return;
		}
		LivingEntity dummy = MorphManager.dummyFor(player);
		if (dummy == null) {
			return;
		}
		MorphManager.copyPose(player, dummy);
		EntityRenderState state = ((EntityRenderDispatcher) (Object) this).extractEntity(dummy, partialTicks);
		state.nameTag = ego.showNametag() ? player.getDisplayName() : null;
		if (state instanceof CreeperRenderState creeperState) {
			// The dummy never ticks, so its own swell stays 0; drive the vanilla
			// swell/flash animation from the synced fuse timer instead.
			float fuseProgress = ClientFuseState.progress(player.getUUID(), partialTicks);
			if (fuseProgress >= 0.0F) {
				creeperState.swelling = fuseProgress;
			}
		}
		cir.setReturnValue(state);
	}
}
