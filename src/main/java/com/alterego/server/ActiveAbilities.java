package com.alterego.server;

import com.alterego.ability.Ability;
import com.alterego.ability.AbilityRegistry;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Server-side execution of active (keypress) abilities. Each returns true on
 * success, which starts the ability's cooldown; the creeper explosion is
 * handled separately in EgoManager because of its fuse.
 */
public final class ActiveAbilities {
	private static final double TELEPORT_RANGE = 32.0;

	private ActiveAbilities() {
	}

	public static boolean use(ServerPlayer player, Ability ability) {
		ServerLevel level = player.level();
		if (ability == AbilityRegistry.TELEPORT) {
			return teleport(player, level);
		}
		if (ability == AbilityRegistry.SMALL_FIREBALL) {
			Vec3 dir = player.getViewVector(1.0F);
			SmallFireball fireball = new SmallFireball(level, player, dir);
			fireball.setPos(player.getX() + dir.x, player.getEyeY() + dir.y, player.getZ() + dir.z);
			level.addFreshEntity(fireball);
			level.levelEvent(null, 1018, player.blockPosition(), 0);
			return true;
		}
		if (ability == AbilityRegistry.LARGE_FIREBALL) {
			Vec3 dir = player.getViewVector(1.0F);
			LargeFireball fireball = new LargeFireball(level, player, dir, 1);
			fireball.setPos(player.getX() + dir.x, player.getEyeY() + dir.y, player.getZ() + dir.z);
			level.addFreshEntity(fireball);
			level.levelEvent(null, 1016, player.blockPosition(), 0);
			return true;
		}
		if (ability == AbilityRegistry.WITHER_SKULL) {
			Vec3 dir = player.getViewVector(1.0F);
			WitherSkull skull = new WitherSkull(level, player, dir);
			skull.setPos(player.getX() + dir.x, player.getEyeY() + dir.y, player.getZ() + dir.z);
			level.addFreshEntity(skull);
			level.levelEvent(null, 1024, player.blockPosition(), 0);
			return true;
		}
		if (ability == AbilityRegistry.SNOWBALL) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW,
					SoundSource.PLAYERS, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
			Projectile.spawnProjectileFromRotation(Snowball::new, level, new ItemStack(Items.SNOWBALL), player,
					0.0F, 1.5F, 1.0F);
			return true;
		}
		if (ability == AbilityRegistry.SPIT) {
			LlamaSpit spit = new LlamaSpit(EntityTypes.LLAMA_SPIT, level);
			spit.setOwner(player);
			spit.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
			Vec3 dir = player.getViewVector(1.0F);
			Projectile.spawnProjectileUsingShoot(spit, level, ItemStack.EMPTY, dir.x, dir.y, dir.z, 1.5F, 3.0F);
			level.playSound(null, player, SoundEvents.LLAMA_SPIT, SoundSource.PLAYERS, 1.0F, 1.0F);
			return true;
		}
		return false;
	}

	private static boolean teleport(ServerPlayer player, ServerLevel level) {
		Vec3 eye = player.getEyePosition(1.0F);
		Vec3 end = eye.add(player.getViewVector(1.0F).scale(TELEPORT_RANGE));
		BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		Vec3 target = hit.getType() == HitResult.Type.MISS
				? end
				: hit.getLocation().add(Vec3.atLowerCornerOf(hit.getDirection().getUnitVec3i()).scale(0.5));

		if (player.isPassenger()) {
			player.stopRiding();
		}
		Vec3 oldPos = player.position();
		if (player.randomTeleport(target.x, target.y, target.z, true)) {
			level.gameEvent(GameEvent.TELEPORT, oldPos, GameEvent.Context.of(player));
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT,
					SoundSource.PLAYERS, 1.0F, 1.0F);
			player.resetFallDistance();
			player.resetCurrentImpulseContext();
			return true;
		}
		return false;
	}
}
