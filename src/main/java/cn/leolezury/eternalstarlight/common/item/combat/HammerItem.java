package cn.leolezury.eternalstarlight.common.item.combat;

import cn.leolezury.eternalstarlight.common.registry.ESCriteriaTriggers;
import cn.leolezury.eternalstarlight.common.util.ESMathUtil;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class HammerItem extends TieredItem {

	private final Supplier<ParticleOptions> smashParticle;
	private final Holder<SoundEvent> smashSound;

	private final int attackDamage;
	private final float attackSpeed;

	public HammerItem(Tier tier, Supplier<ParticleOptions> smashParticle, Holder<SoundEvent> smashSound,
					  int attackDamage, float attackSpeed, Properties properties) {
		super(tier, properties);
		this.smashParticle = smashParticle;
		this.smashSound = smashSound;
		this.attackDamage = attackDamage + (int) tier.getAttackDamageBonus();
		this.attackSpeed = attackSpeed;
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
		if (slot == EquipmentSlot.MAINHAND) {
			ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();

			builder.put(
				Attributes.ATTACK_DAMAGE,
				new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "hammer_damage", this.attackDamage, AttributeModifier.Operation.ADDITION)
			);

			builder.put(
				Attributes.ATTACK_SPEED,
				new AttributeModifier(BASE_ATTACK_SPEED_UUID, "hammer_speed", this.attackSpeed, AttributeModifier.Operation.ADDITION)
			);

			return builder.build();
		}

		return super.getDefaultAttributeModifiers(slot);
	}

	protected void spawnBlockParticle(ServerLevel serverLevel, BlockPos pos, Vec3 particlePos) {
		BlockState state = serverLevel.getBlockState(pos.below());
		if (state.getRenderShape() != RenderShape.INVISIBLE) {
			serverLevel.sendParticles(
				new BlockParticleOption(ParticleTypes.BLOCK, state),
				particlePos.x, particlePos.y, particlePos.z,
				3, 0.15, 0.15, 0.15, 0.2
			);
		}
	}

	public void performCriticalAttack(Player player, Entity target) {
		if (player instanceof ServerPlayer serverPlayer) {
			ESCriteriaTriggers.HAMMER_CRITICAL_HIT.trigger(serverPlayer);
		}

		Level level = player.level();

		for (LivingEntity entity : level.getNearbyEntities(
			LivingEntity.class,
			TargetingConditions.DEFAULT,
			player,
			new AABB(target.blockPosition()).inflate(2))) {

			if (entity.hurt(level.damageSources().playerAttack(player),
				(float) (player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.75))) {

				entity.addDeltaMovement(new Vec3(
					0.0,
					0.4 * Math.max(0.0, 1.0 - entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)),
					0.0
				));
			}
		}

		level.playSound(null, target.blockPosition(), smashSound.value(), player.getSoundSource());

		if (target.onGround() && level instanceof ServerLevel serverLevel) {
			for (int i = 0; i < 360; i += 10) {
				Vec3 vec3 = ESMathUtil.rotationToPosition(
					target.getOnPos().getCenter().add(0, -0.1, 0),
					1.75f, 0, i
				);

				BlockPos particlePos = new BlockPos((int) vec3.x, (int) vec3.y, (int) vec3.z);

				for (int j = 0; j < 5; j++) {
					spawnBlockParticle(serverLevel, particlePos, vec3.add(0, 0.6, 0));
				}

				if (smashParticle.get() != null) {
					Vec3 particleVec = vec3.add(0, 0.6, 0);
					serverLevel.sendParticles(
						smashParticle.get(),
						particleVec.x, particleVec.y, particleVec.z,
						3, 0.15, 0.15, 0.15, 0.1
					);
				}
			}
		}
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity entity, LivingEntity attacker) {
		stack.hurtAndBreak(1, attacker, (e) -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
		return true;
	}
}