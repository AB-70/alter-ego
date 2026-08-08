package com.alterego.client.mixin;

import net.minecraft.world.entity.WalkAnimationState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * WalkAnimationState keeps its limb-swing phase in private fields with no
 * copy method; these accessors let MorphManager mirror a player's exact limb
 * phase onto the morph dummy.
 */
@Mixin(WalkAnimationState.class)
public interface WalkAnimationStateAccessor {
	@Accessor("speedOld")
	float alterego$getSpeedOld();

	@Accessor("speedOld")
	void alterego$setSpeedOld(float speedOld);

	@Accessor("speed")
	float alterego$getSpeed();

	@Accessor("speed")
	void alterego$setSpeed(float speed);

	@Accessor("position")
	float alterego$getPosition();

	@Accessor("position")
	void alterego$setPosition(float position);

	@Accessor("positionScale")
	float alterego$getPositionScale();

	@Accessor("positionScale")
	void alterego$setPositionScale(float positionScale);
}
