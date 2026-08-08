package com.alterego.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.alterego.AlterEgo;
import com.alterego.EgoSelection;
import com.alterego.client.mixin.WalkAnimationStateAccessor;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.BlockItem;

import org.jetbrains.annotations.Nullable;

/**
 * Maintains the client-side dummy entities that stand in for morphed players
 * during rendering. Dummies never join the level; each frame we mirror the
 * player's pose onto them and hand them to the entity renderer instead of the
 * player.
 */
public final class MorphManager {
	/** Only the humanoid slots — animal-only slots (body/saddle) reject items on many mobs. */
	private static final EquipmentSlot[] HUMANOID_SLOTS = {
			EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
			EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
	};

	private static final Map<UUID, LivingEntity> DUMMIES = new HashMap<>();
	/**
	 * Dummies need entity IDs (getId() throws unassigned) but must never clash
	 * with real entities or the picker screen's previews, hence a far-negative
	 * range.
	 */
	private static int nextDummyId = -1_000_000;

	private MorphManager() {
	}

	@Nullable
	public static LivingEntity dummyFor(AbstractClientPlayer player) {
		EgoSelection ego = ClientEgoState.get(player.getUUID());
		if (ego.isSelf()) {
			DUMMIES.remove(player.getUUID());
			return null;
		}
		LivingEntity dummy = DUMMIES.get(player.getUUID());
		if (dummy == null || dummy.getType() != ego.entityType() || dummy.level() != player.level()) {
			dummy = create(ego.entityType(), player);
			if (dummy == null) {
				return null;
			}
			DUMMIES.put(player.getUUID(), dummy);
		}
		return dummy;
	}

	@Nullable
	private static LivingEntity create(EntityType<?> type, AbstractClientPlayer player) {
		try {
			if (type.create(player.level(), new EntitySpawnRequest(EntitySpawnReason.LOAD, true)) instanceof LivingEntity living) {
				living.setId(nextDummyId--);
				living.setSilent(true);
				return living;
			}
		} catch (Exception e) {
			AlterEgo.LOGGER.warn("Failed to create morph dummy for {}", EntityType.getKey(type), e);
		}
		return null;
	}

	/** Mirrors the player's current pose onto the dummy for this frame. */
	public static void copyPose(AbstractClientPlayer player, LivingEntity dummy) {
		dummy.setPos(player.getX(), player.getY(), player.getZ());
		// Render interpolation reads xOld/yOld/zOld, but light sampling goes
		// through getPosition(partial) which lerps the xo/yo/zo set — both must
		// track the player or the morph renders at the wrong brightness.
		dummy.xOld = player.xOld;
		dummy.yOld = player.yOld;
		dummy.zOld = player.zOld;
		dummy.xo = player.xo;
		dummy.yo = player.yo;
		dummy.zo = player.zo;

		dummy.setYRot(player.getYRot());
		dummy.yRotO = player.yRotO;
		dummy.setXRot(player.getXRot());
		dummy.xRotO = player.xRotO;
		dummy.yBodyRot = player.yBodyRot;
		dummy.yBodyRotO = player.yBodyRotO;
		dummy.yHeadRot = player.yHeadRot;
		dummy.yHeadRotO = player.yHeadRotO;

		dummy.tickCount = player.tickCount;
		dummy.setPose(player.getPose());
		dummy.setSprinting(player.isSprinting());
		dummy.setShiftKeyDown(player.isShiftKeyDown());
		dummy.setInvisible(player.isInvisible());
		dummy.setOnGround(player.onGround());

		dummy.attackAnim = player.attackAnim;
		dummy.swinging = player.swinging;
		dummy.hurtTime = player.hurtTime;

		// Mirror equipment so humanoid morphs (skeleton, zombie...) visibly hold
		// the player's items and wear their armor.
		for (EquipmentSlot slot : HUMANOID_SLOTS) {
			dummy.setItemSlot(slot, player.getItemBySlot(slot));
		}
		if (dummy instanceof Mob mob) {
			// Drives attack/aiming poses, e.g. the skeleton's drawn-bow arms
			// while the player charges a bow.
			mob.setAggressive(player.isUsingItem());
		}
		if (dummy instanceof EnderMan enderMan) {
			// Endermen render a carried block instead of a held item.
			enderMan.setCarriedBlock(player.getMainHandItem().getItem() instanceof BlockItem blockItem
					? blockItem.getBlock().defaultBlockState()
					: null);
		}

		WalkAnimationStateAccessor from = (WalkAnimationStateAccessor) player.walkAnimation;
		WalkAnimationStateAccessor to = (WalkAnimationStateAccessor) dummy.walkAnimation;
		to.alterego$setSpeedOld(from.alterego$getSpeedOld());
		to.alterego$setSpeed(from.alterego$getSpeed());
		to.alterego$setPosition(from.alterego$getPosition());
		to.alterego$setPositionScale(from.alterego$getPositionScale());
	}

	public static void clear() {
		DUMMIES.clear();
	}
}
