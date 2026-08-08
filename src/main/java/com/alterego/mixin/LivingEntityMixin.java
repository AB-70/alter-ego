package com.alterego.mixin;

import com.alterego.EgoSelection;
import com.alterego.EgoSideHooks;
import com.alterego.ability.AbilityRegistry;
import com.alterego.server.EgoManager;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Spider-morph wall climbing: players with the wall-climb passive treat any
 * wall they push against as climbable, reusing vanilla ladder physics
 * (including sneak-to-hold). Applied on both logical sides so client
 * prediction and server movement agree.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Inject(method = "onClimbable", at = @At("HEAD"), cancellable = true)
	private void alterego$wallClimb(CallbackInfoReturnable<Boolean> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self instanceof Player player && self.horizontalCollision) {
			EgoSelection ego = player.level().isClientSide()
					? EgoSideHooks.clientEgoLookup.apply(player.getUUID())
					: EgoManager.get(player.getUUID());
			if (ego != null && !ego.isSelf() && ego.enabledAbilities().contains(AbilityRegistry.WALL_CLIMB.id())) {
				cir.setReturnValue(true);
			}
		}
	}
}
