package cn.leolezury.eternalstarlight.common.entity.living.monster;

import cn.leolezury.eternalstarlight.common.config.ESConfig;
import cn.leolezury.eternalstarlight.common.entity.living.phase.BehaviorManager;
import cn.leolezury.eternalstarlight.common.entity.living.phase.MeleeAttackPhase;
import cn.leolezury.eternalstarlight.common.entity.living.phase.MultiBehaviorUser;
import cn.leolezury.eternalstarlight.common.registry.ESEntities;
import cn.leolezury.eternalstarlight.common.registry.ESItems;
import cn.leolezury.eternalstarlight.common.util.ESTags;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Tangled extends Monster implements MultiBehaviorUser {
	private static final String TAG_VARIANT = "variant";
	private static final int MELEE_ID = 1;

	public AnimationState idleAnimationState = new AnimationState();
	public AnimationState meleeAnimationState = new AnimationState();

	protected static final EntityDataAccessor<Integer> BEHAVIOR_STATE = SynchedEntityData.defineId(Tangled.class, EntityDataSerializers.INT);

	public int getBehaviorState() {
		return this.getEntityData().get(BEHAVIOR_STATE);
	}

	public void setBehaviorState(int attackState) {
		this.getEntityData().set(BEHAVIOR_STATE, attackState);
	}

	protected static final EntityDataAccessor<Integer> BEHAVIOR_TICKS = SynchedEntityData.defineId(Tangled.class, EntityDataSerializers.INT);

	public int getBehaviorTicks() {
		return this.getEntityData().get(BEHAVIOR_TICKS);
	}

	public void setBehaviorTicks(int behaviourTicks) {
		this.getEntityData().set(BEHAVIOR_TICKS, behaviourTicks);
	}

	protected static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(Tangled.class, EntityDataSerializers.INT);

	public int getVariant() {
		return this.getEntityData().get(VARIANT) % 3;
	}

	public void setVariant(int variant) {
		this.getEntityData().set(VARIANT, variant % 3);
	}

	private final BehaviorManager<Tangled> behaviorManager = new BehaviorManager<>(this, List.of(
		new MeleeAttackPhase<Tangled>(MELEE_ID, 1, 20, 10).with(2, 15)
	));

	@Override
	public BehaviorManager<Tangled> getBehaviorManager() {
		return behaviorManager;
	}

	public Tangled(EntityType<? extends Tangled> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(BEHAVIOR_STATE, 0);
		this.entityData.define(BEHAVIOR_TICKS, 0);
		this.entityData.define(VARIANT, 0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false) {
			@Override
			protected void checkAndPerformAttack(LivingEntity target, double distanceToTarget) {

			}
		});
		this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0, 0.0F));
		this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(0, new HurtByTargetGoal(this).setAlertOthers());
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, 1.0)
			.add(Attributes.ARMOR, 0.0)
			.add(Attributes.ATTACK_DAMAGE, 1.0)
			.add(Attributes.FOLLOW_RANGE, 16.0)
			.add(Attributes.MOVEMENT_SPEED, 0.2);
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag dataTag) {
		SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, dataTag);
		setVariant(random.nextInt(3));

		this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(ESConfig.tangled.maxHealth.get());
		this.getAttribute(Attributes.ARMOR).setBaseValue(ESConfig.tangled.armor.get());
		this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(ESConfig.tangled.attackDamage.get());
		this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(ESConfig.tangled.followRange.get());

		this.setHealth(this.getMaxHealth());

		return data;
	}

	@Override
	protected void customServerAiStep() {
		super.customServerAiStep();
		if (getTarget() != null && !getTarget().isAlive()) {
			setTarget(null);
		}
		if (!isNoAi() && isAlive()) {
			this.behaviorManager.tick();
		}
	}

	@Override
	public boolean doHurtTarget(Entity entity) {
		boolean flag = super.doHurtTarget(entity);
		if (flag && entity instanceof LivingEntity living && getRandom().nextInt(5) == 0 && !living.hasEffect(MobEffects.POISON)) {
			living.addEffect(new MobEffectInstance(MobEffects.POISON, 40));
		}
		return flag;
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide) {
			idleAnimationState.startIfStopped(tickCount);
		}
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
		if (accessor.equals(BEHAVIOR_STATE)) {
			if (getBehaviorState() == MELEE_ID) {
				meleeAnimationState.start(tickCount);
			} else {
				meleeAnimationState.stop();
			}
		}
		super.onSyncedDataUpdated(accessor);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains(TAG_VARIANT, CompoundTag.TAG_INT)) {
			setVariant(compoundTag.getInt(TAG_VARIANT));
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt(TAG_VARIANT, getVariant());
	}

	@Override
	public double getPassengersRidingOffset() {
		return -0.7F;
	}

	@Override
	public float getStandingEyeHeight(Pose pose, EntityDimensions size) {
		return 1.74F;
	}

	@Override
	protected void tickDeath() {
		if (!level().isClientSide && this.deathTime == 0 && getRandom().nextBoolean() && ESConfig.tangledSkull.canSpawn.get()) {
			TangledSkull skull = new TangledSkull(ESEntities.TANGLED_SKULL.get(), level());
			skull.setPos(getX(), getY(0.75), getZ());
			skull.setTarget(getTarget());
			skull.setLastHurtByMob(getTarget());
			level().addFreshEntity(skull);
		}
		++this.deathTime;
		if (this.deathTime >= 20 && !this.level().isClientSide() && !this.isRemoved()) {
			this.level().broadcastEntityEvent(this, (byte) 60);
			this.remove(RemovalReason.KILLED);
		}
	}

	@Override
	protected void dropCustomDeathLoot(DamageSource source, int lootingLevel, boolean recentlyHit) {
		super.dropCustomDeathLoot(source, lootingLevel, recentlyHit);

		Entity entity = source.getEntity();
		if (entity instanceof Creeper creeper) {
			if (creeper.canDropMobsSkull()) {
				ItemStack itemStack = ESItems.TANGLED_SKULL.get().getDefaultInstance();
				creeper.increaseDroppedSkulls();
				this.spawnAtLocation(itemStack);
			}
		}
	}

	@Override
	public boolean isAlliedTo(Entity entity) {
		return super.isAlliedTo(entity) || entity.getType().is(ESTags.EntityTypes.LUNAR_MONSTROSITY_ALLIES);
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.SKELETON_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.SKELETON_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.SKELETON_DEATH;
	}

	public static boolean checkTangledSpawnRules(EntityType<? extends Tangled> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
		return checkAnyLightMonsterSpawnRules(type, level, spawnType, pos, random) && ESConfig.tangled.canSpawn.get();
	}
}
