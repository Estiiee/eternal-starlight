package cn.leolezury.eternalstarlight.common.entity.living.boss.gatekeeper;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.block.entity.LootChestBlockEntity;
import cn.leolezury.eternalstarlight.common.config.ESConfig;
import cn.leolezury.eternalstarlight.common.data.ESCrests;
import cn.leolezury.eternalstarlight.common.data.ESPaintingVariants;
import cn.leolezury.eternalstarlight.common.entity.living.boss.ESBoss;
import cn.leolezury.eternalstarlight.common.entity.living.boss.ESServerBossEvent;
import cn.leolezury.eternalstarlight.common.entity.living.goal.GatekeeperTargetGoal;
import cn.leolezury.eternalstarlight.common.entity.living.goal.LookAtTargetGoal;
import cn.leolezury.eternalstarlight.common.entity.living.phase.BehaviorManager;
import cn.leolezury.eternalstarlight.common.handler.ESCommonHandler;
import cn.leolezury.eternalstarlight.common.network.OpenGatekeeperGuiPacket;
import cn.leolezury.eternalstarlight.common.network.ParticlePacket;
import cn.leolezury.eternalstarlight.common.particle.ExplosionShockParticleOptions;
import cn.leolezury.eternalstarlight.common.platform.ESPlatform;
import cn.leolezury.eternalstarlight.common.registry.*;
import cn.leolezury.eternalstarlight.common.util.ESBookUtil;
import cn.leolezury.eternalstarlight.common.util.ESCrestUtil;
import cn.leolezury.eternalstarlight.common.util.ESEntityUtil;
import cn.leolezury.eternalstarlight.common.util.ModelSnapshot;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TheGatekeeper extends ESBoss implements Npc, Merchant {
	public static final int GUI_RESPONSE_EMPTY = 0;
	public static final int GUI_RESPONSE_CHALLENGE = 1;
	public static final int GUI_RESPONSE_TRADE = 2;
	public static final int GUI_RESPONSE_LEAVE = 3;
	public static final byte EVENT_TALK = 100;
	private static final byte EVENT_BLOCK = 101;
	private static final String TAG_OFFERS = "offers";
	private static final String TAG_GATEKEEPER_NAME = "gatekeeper_name";
	private static final String TAG_FIGHT_TARGET = "fight_target";
	private static final String TAG_STANDARD_FIGHT = "standard_fight";
	private static final String TAG_RESTOCK_COOLDOWN = "restock_cooldown";
	private int noTargetTime;
	private final List<BlockPos> recentPositions = new ArrayList<>();
	public boolean healInterrupted = false, healInterruptedIndirect = false;
	public int healCount, healInterruptedCount;

	protected static final EntityDataAccessor<Direction> STAND_DIRECTION = SynchedEntityData.defineId(TheGatekeeper.class, EntityDataSerializers.DIRECTION);

	public Direction getStandDirection() {
		return this.getEntityData().get(STAND_DIRECTION);
	}

	public void setStandDirection(Direction standDirection) {
		this.getEntityData().set(STAND_DIRECTION, standDirection);
	}

	public TheGatekeeper(EntityType<? extends TheGatekeeper> entityType, Level level) {
		super(entityType, level);
		this.noCulling = true;
	}

	private final ESServerBossEvent bossEvent = new ESServerBossEvent(this, ESServerBossEvent.THE_GATEKEEPER, BossEvent.BossBarColor.WHITE, false);

	private final BehaviorManager<TheGatekeeper> behaviorManager = new BehaviorManager<>(this, List.of(
		new GatekeeperStepBackPhase(),
		new GatekeeperJumpStartPhase(),
		new GatekeeperJumpTransitionPhase(),
		new GatekeeperJumpEndPhase(),
		new GatekeeperGreatswordPhase(),
		new GatekeeperHammerPhase(),
		new GatekeeperDashPhase(),
		new GatekeeperGreatswordComboPhase(),
		new GatekeeperBowPhase(),
		new GatekeeperBowComboPhase(),
		new GatekeeperCastFireballPhase(),
		new GatekeeperTeleportPhase(),
		new GatekeeperEatPhase(),
		new GatekeeperEatFailPhase()
	));

	@Override
	public BehaviorManager<TheGatekeeper> getBehaviorManager() {
		return behaviorManager;
	}

	public AnimationState sitAnimationState = new AnimationState();
	public AnimationState standAnimationState = new AnimationState();
	public AnimationState talkAnimationState = new AnimationState();
	public AnimationState stepBackAnimationState = new AnimationState();
	public AnimationState jumpStartAnimationState = new AnimationState();
	public AnimationState jumpTransitionAnimationState = new AnimationState();
	public AnimationState jumpEndAnimationState = new AnimationState();
	public AnimationState greatswordAnimationState = new AnimationState();
	public AnimationState hammerAnimationState = new AnimationState();
	public AnimationState dashAnimationState = new AnimationState();
	public AnimationState greatswordComboAnimationState = new AnimationState();
	public AnimationState bowAnimationState = new AnimationState();
	public AnimationState bowComboAnimationState = new AnimationState();
	public AnimationState castFireballAnimationState = new AnimationState();
	public AnimationState teleportAnimationState = new AnimationState();
	public AnimationState blockAnimationState = new AnimationState();
	public AnimationState eatAnimationState = new AnimationState();
	public AnimationState eatFailAnimationState = new AnimationState();

	public final List<Pair<Vec3, ModelSnapshot>> trailSnapshots = new ArrayList<>(50);
	public float lastTrailTick = 0;

	public boolean shouldAddTrailSnapshot() {
		return Mth.degreesDifferenceAbs(getYRot(), yBodyRot) < 30
			&& Mth.degreesDifferenceAbs(getYRot(), yBodyRotO) < 30
			&& Mth.degreesDifferenceAbs(yBodyRot, yBodyRotO) < 30
			&& ((getBehaviorState() == GatekeeperJumpStartPhase.ID && getBehaviorTicks() >= 15)
			|| getBehaviorState() == GatekeeperJumpTransitionPhase.ID
			|| (getBehaviorState() == GatekeeperDashPhase.ID && getBehaviorTicks() > 16 && getBehaviorTicks() < 25));
	}

	private String gatekeeperName = "Gatekeeper";
	@Nullable
	private Player customer;
	@Nullable
	protected MerchantOffers offers;
	private int restockCooldown;
	@Nullable
	private ServerPlayer conversationTarget;
	@Nullable
	private String fightTarget;
	private boolean standardFight = true;

	public void setStandardFight(boolean standardFight) {
		this.standardFight = standardFight;
	}

	public boolean isStandardFight() {
		return standardFight;
	}

	public void setFightTargetName(String fightTarget) {
		this.fightTarget = fightTarget;
	}

	public Optional<? extends Player> getFightTarget() {
		return level().players().stream().filter(p -> p.getName().getString().equals(fightTarget)).findFirst();
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(STAND_DIRECTION, Direction.UP);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);

		if (tag.contains(TAG_OFFERS, CompoundTag.TAG_COMPOUND)) {
			this.offers = new MerchantOffers(tag.getCompound(TAG_OFFERS));
		}

		gatekeeperName = tag.getString(TAG_GATEKEEPER_NAME);
		fightTarget = tag.getString(TAG_FIGHT_TARGET);

		if (tag.contains(TAG_STANDARD_FIGHT)) {
			standardFight = tag.getBoolean(TAG_STANDARD_FIGHT);
		}

		restockCooldown = tag.getInt(TAG_RESTOCK_COOLDOWN);

		if (this.offers == null) {
			this.offers = new MerchantOffers();
			this.addTrades();
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);

		if (!this.level().isClientSide) {
			MerchantOffers offers = this.getOffers();
			if (!offers.isEmpty()) {
				tag.put(TAG_OFFERS, offers.createTag());
			}
		}

		tag.putString(TAG_GATEKEEPER_NAME, gatekeeperName);

		if (fightTarget != null) {
			tag.putString(TAG_FIGHT_TARGET, fightTarget);
		}

		tag.putBoolean(TAG_STANDARD_FIGHT, standardFight);
		tag.putInt(TAG_RESTOCK_COOLDOWN, restockCooldown);
	}

	@Override
	public void startSeenByPlayer(ServerPlayer serverPlayer) {
		super.startSeenByPlayer(serverPlayer);
		bossEvent.addPlayer(serverPlayer);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer serverPlayer) {
		super.stopSeenByPlayer(serverPlayer);
		bossEvent.removePlayer(serverPlayer);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new MeleeAttackGoal(this, 0.5D, false) {
			@Override
			protected void checkAndPerformAttack(LivingEntity livingEntity, double d) {

			}
		});
		goalSelector.addGoal(2, new LookAtTargetGoal(this));
		goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
		goalSelector.addGoal(4, new LookAtPlayerGoal(this, Mob.class, 8.0F));

		targetSelector.addGoal(0, new GatekeeperTargetGoal(this) {
			@Override
			public boolean canUse() {
				return super.canUse() && standardFight && isActivated();
			}
		});
		targetSelector.addGoal(1, new HurtByTargetGoal(this, TheGatekeeper.class) {
			@Override
			public boolean canUse() {
				return super.canUse() && !standardFight && isActivated();
			}
		}.setAlertOthers());
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true) {
			@Override
			public boolean canUse() {
				return super.canUse() && !standardFight && isActivated();
			}
		});
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, 1.0)
			.add(Attributes.ARMOR, 0.0)
			.add(Attributes.ATTACK_DAMAGE, 1.0)
			.add(Attributes.FOLLOW_RANGE, 16.0)
			.add(Attributes.MOVEMENT_SPEED, 0.6)
			.add(Attributes.ARMOR_TOUGHNESS, 5.0)
			.add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag dataTag) {
		spawnGroupData = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, dataTag);

		RandomSource random = level.getRandom();
		this.populateDefaultEquipmentSlots(random, difficulty);

		this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(ESConfig.theGatekeeper.maxHealth.get());
		this.getAttribute(Attributes.ARMOR).setBaseValue(ESConfig.theGatekeeper.armor.get());
		this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(ESConfig.theGatekeeper.attackDamage.get());
		this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(ESConfig.theGatekeeper.followRange.get());

		this.setHealth(this.getMaxHealth());

		return spawnGroupData;
	}

	@Override
	protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
		super.populateDefaultEquipmentSlots(randomSource, difficultyInstance);
		Arrays.fill(this.handDropChances, 0);
		this.setItemSlot(EquipmentSlot.MAINHAND, ESItems.GLISTERING_SWORD.get().getDefaultInstance());
	}

	@Override
	protected BodyRotationControl createBodyControl() {
		return new BodyRotationControl(this) {
			@Override
			public void clientTick() {
				if (standAnimationState.isStarted()) {
					TheGatekeeper.this.yBodyRot = Mth.wrapDegrees(TheGatekeeper.this.getStandDirection().toYRot());
					TheGatekeeper.this.setYRot(Mth.wrapDegrees(TheGatekeeper.this.getStandDirection().toYRot()));
					TheGatekeeper.this.yHeadRot = Mth.rotateIfNecessary(TheGatekeeper.this.yHeadRot, TheGatekeeper.this.yBodyRot, TheGatekeeper.this.getMaxHeadYRot());
				} else {
					super.clientTick();
				}
			}
		};
	}

	@Override
	public int getUseItemRemainingTicks() {
		if (getBehaviorState() == GatekeeperBowPhase.ID
			&& getBehaviorTicks() >= 6
			&& getBehaviorTicks() <= 26) {
			int useDuration = getMainHandItem().getUseDuration();
			return useDuration - (getBehaviorTicks() - 6);
		}
		if (getBehaviorState() == GatekeeperBowComboPhase.ID) {
			int useDuration = getMainHandItem().getUseDuration();
			if (getBehaviorTicks() >= 5 && getBehaviorTicks() <= 20) {
				return useDuration - Math.round((getBehaviorTicks() - 5) * 1.3f);
			}
			if (getBehaviorTicks() >= 30 && getBehaviorTicks() <= 38) {
				return useDuration - Math.round((getBehaviorTicks() - 30) * 2.5f);
			}
			if (getBehaviorTicks() >= 47 && getBehaviorTicks() <= 55) {
				return useDuration - Math.round((getBehaviorTicks() - 47) * 2.5f);
			}
		}
		return super.getUseItemRemainingTicks();
	}

	@Override
	public boolean isUsingItem() {
		if (getBehaviorState() == GatekeeperBowPhase.ID
			&& getBehaviorTicks() >= 6
			&& getBehaviorTicks() <= 26) {
			return true;
		}
		if (getBehaviorState() == GatekeeperBowComboPhase.ID
			&& ((getBehaviorTicks() >= 5 && getBehaviorTicks() <= 20)
			|| (getBehaviorTicks() >= 30 && getBehaviorTicks() <= 38)
			|| (getBehaviorTicks() >= 47 && getBehaviorTicks() <= 55))) {
			return true;
		}
		return super.isUsingItem();
	}

	@Override
	public ItemStack getUseItem() {
		if (getBehaviorState() == GatekeeperBowPhase.ID
			&& getBehaviorTicks() >= 6
			&& getBehaviorTicks() <= 26) {
			return getMainHandItem();
		}
		if (getBehaviorState() == GatekeeperBowComboPhase.ID
			&& ((getBehaviorTicks() >= 5 && getBehaviorTicks() <= 20)
			|| (getBehaviorTicks() >= 30 && getBehaviorTicks() <= 38)
			|| (getBehaviorTicks() >= 47 && getBehaviorTicks() <= 55))) {
			return getMainHandItem();
		}
		return super.getUseItem();
	}

	@Override
	public void handleEntityEvent(byte b) {
		if (b == EVENT_TALK) {
			talkAnimationState.start(tickCount);
		} else if (b == EVENT_BLOCK) {
			blockAnimationState.start(tickCount);
		} else {
			super.handleEntityEvent(b);
		}
	}

	public void stopAllAnimStates() {
		talkAnimationState.stop();
		stepBackAnimationState.stop();
		jumpStartAnimationState.stop();
		jumpTransitionAnimationState.stop();
		jumpEndAnimationState.stop();
		greatswordAnimationState.stop();
		hammerAnimationState.stop();
		dashAnimationState.stop();
		greatswordComboAnimationState.stop();
		bowAnimationState.stop();
		bowComboAnimationState.stop();
		castFireballAnimationState.stop();
		teleportAnimationState.stop();
		blockAnimationState.stop();
		eatAnimationState.stop();
		eatFailAnimationState.stop();
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
		if (accessor.equals(BEHAVIOR_STATE)) {
			stopAllAnimStates();
			switch (getBehaviorState()) {
				case GatekeeperStepBackPhase.ID -> stepBackAnimationState.start(tickCount);
				case GatekeeperJumpStartPhase.ID -> jumpStartAnimationState.start(tickCount);
				case GatekeeperJumpTransitionPhase.ID -> jumpTransitionAnimationState.start(tickCount);
				case GatekeeperJumpEndPhase.ID -> jumpEndAnimationState.start(tickCount);
				case GatekeeperGreatswordPhase.ID -> greatswordAnimationState.start(tickCount);
				case GatekeeperHammerPhase.ID -> hammerAnimationState.start(tickCount);
				case GatekeeperDashPhase.ID -> dashAnimationState.start(tickCount);
				case GatekeeperGreatswordComboPhase.ID -> greatswordComboAnimationState.start(tickCount);
				case GatekeeperBowPhase.ID -> bowAnimationState.start(tickCount);
				case GatekeeperBowComboPhase.ID -> bowComboAnimationState.start(tickCount);
				case GatekeeperCastFireballPhase.ID -> castFireballAnimationState.start(tickCount);
				case GatekeeperTeleportPhase.ID -> teleportAnimationState.start(tickCount);
				case GatekeeperEatPhase.ID -> eatAnimationState.start(tickCount);
				case GatekeeperEatFailPhase.ID -> eatFailAnimationState.start(tickCount);
			}
		}
		super.onSyncedDataUpdated(accessor);
	}

	@Override
	public boolean canAttack(LivingEntity livingEntity) {
		return super.canAttack(livingEntity) && isActivated();
	}

	@Override
	public void initializeBoss() {
		super.initializeBoss();
		setActivated(false);
		gatekeeperName = ESCommonHandler.getGatekeeperName();
	}

	@Override
	public boolean shouldPlayBossMusic() {
		return super.shouldPlayBossMusic() && isActivated();
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		if (!level().isClientSide && player instanceof ServerPlayer serverPlayer && serverPlayer.getServer() != null && getTradingPlayer() == null && !isActivated() && getTarget() == null && getFightTarget().isEmpty()) {
			Advancement killDragon = serverPlayer.getServer().getAdvancements().getAdvancement(new ResourceLocation("minecraft:end/kill_dragon"));
			if (killDragon != null && serverPlayer.getAdvancements().getOrStartProgress(killDragon).isDone() && !isPlayerPermitted(serverPlayer)) {
				permitPlayer(serverPlayer);
				ItemStack lootBag = getBossLootBag();
				ItemEntity item = player.spawnAtLocation(lootBag);
				if (item != null) {
					ESDataAttachments.IMPORTANT_ITEM.setData(item, true);
					item.setTarget(player.getUUID());
					item.setGlowingTag(true);
					item.setExtendedLifetime();
				}
			}
			conversationTarget = serverPlayer;
			ESPlatform.INSTANCE.sendToClient(serverPlayer, new OpenGatekeeperGuiPacket(getId(), isPlayerPermitted(serverPlayer)));
		}
		return isActivated() ? InteractionResult.PASS : InteractionResult.sidedSuccess(level().isClientSide);
	}

	public synchronized void handleDialogueClose(int operation) {
		if (conversationTarget != null) {
			if (operation == GUI_RESPONSE_CHALLENGE) {
				standardFight = true;
				setFightTargetName(conversationTarget.getName().getString());
				setTarget(conversationTarget);
				setActivated(true);
			}
			if (operation == GUI_RESPONSE_TRADE && isPlayerPermitted(conversationTarget) && offers != null && !offers.isEmpty()) {
				this.setTradingPlayer(conversationTarget);
				this.openTradingScreen(conversationTarget, this.getDisplayName(), 1);
			}
			if (operation == GUI_RESPONSE_LEAVE) {
				if (level() instanceof ServerLevel serverLevel) {
					RandomSource random = serverLevel.getRandom();
					for (int i = 0; i <= 25; i++) {
						ESPlatform.INSTANCE.sendToAllClients(serverLevel, new ParticlePacket(ExplosionShockParticleOptions.DEATH, getX() + (random.nextFloat() - 0.5f) * getBbWidth() * 3, getY(), getZ() + (random.nextFloat() - 0.5f) * getBbWidth() * 3, 0, 1, 0));
					}
				}
				discard();
			}
			conversationTarget = null;
		}
	}

	private static void permitPlayer(ServerPlayer player) {
		ESCriteriaTriggers.CHALLENGED_GATEKEEPER.trigger(player);
		ESBookUtil.unlock(player, EternalStarlight.id("permitted_by_gatekeeper"));
	}

	public static boolean isPlayerPermitted(ServerPlayer player) {
		if (player.getServer() != null) {
			Advancement challenge = player.getServer().getAdvancements().getAdvancement(EternalStarlight.id("challenge_gatekeeper"));
			boolean challenged = challenge != null && player.getAdvancements().getOrStartProgress(challenge).isDone();
			if (challenged) {
				return true;
			}
		}
		return ESBookUtil.getUnlockedParts(player).contains(EternalStarlight.id("permitted_by_gatekeeper"));
	}

	@Override
	public void die(DamageSource source) {
		if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || !standardFight) {
			super.die(source);
		} else if (!level().isClientSide) {
			getFightTarget().ifPresent(p -> {
				if (p instanceof ServerPlayer serverPlayer) {
					permitPlayer(serverPlayer);
				}
			});
			if (source.getEntity() instanceof ServerPlayer player && !fightParticipants.contains(player.getUUID())) {
				fightParticipants.add(player.getUUID());
			}
			bossDefeat();
			abortFight();
		}
	}

	@Override
	protected void tickDeath() {
		super.tickDeath();
		if (deathTime == 0) {
			stopAllAnimStates();
			setBehaviorState(0);
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && source.getEntity() == this) {
			return false;
		}
		if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !(ESConfig.theGatekeeper.canAlwaysHurtWhenFighting.get() && isActivated()) && (source.getEntity() == null || getTarget() == null || getBehaviorState() == 0)) {
			if (isActivated()) {
				if (getBehaviorState() == 0 && source.getEntity() != null) {
					level().broadcastEntityEvent(this, EVENT_BLOCK);
				}
				if (level() instanceof ServerLevel serverLevel && amount >= 7) {
					serverLevel.sendParticles(ESParticles.PARRY.get(), getX(), getY() + getBbHeight() / 2, getZ(), 8, 0, 0, 0, 0.2 + getRandom().nextFloat() * 0.2);
					playSound(ESSoundEvents.THE_GATEKEEPER_PARRY.get(), getSoundVolume(), getVoicePitch());
				}
			}
			return false;
		}
		if (getBehaviorState() == GatekeeperEatPhase.ID && source.getEntity() != null) {
			healInterrupted = true;
			healInterruptedIndirect = source.getDirectEntity() == null;
		}
		return super.hurt(source, amount);
	}

	@Override
	public boolean canBeSeenAsEnemy() {
		return isActivated() && super.canBeSeenAsEnemy();
	}

	private void tryTeleportBack() {
		if (getInitialPos().dimension() != level().dimension()) {
			return;
		}

		Vec3 initialPos = getInitialPos().pos();
		BlockPos blockPos = BlockPos.containing(initialPos);

		if (initialPos.distanceTo(position()) > 15) {

			if (level().noCollision(this, getBoundingBox().move(Vec3.atBottomCenterOf(blockPos).subtract(position())))) {
				setPos(Vec3.atBottomCenterOf(blockPos));
				return;
			}

			for (int i = 0; i < 64; i++) {
				BlockPos startPos = blockPos.offset(
					getRandom().nextInt(31) - 15,
					getRandom().nextInt(31) - 15,
					getRandom().nextInt(31) - 15
				);

				boolean successful = false;
				double finalY = startPos.getY();

				if (level().getBlockState(startPos).isAir()) {
					BlockHitResult result = level().clip(new ClipContext(
						Vec3.atCenterOf(startPos),
						Vec3.atCenterOf(startPos).add(0, -15, 0),
						ClipContext.Block.COLLIDER,
						ClipContext.Fluid.NONE,
						this
					));

					if (result.getType() != HitResult.Type.MISS) {
						finalY = result.getLocation().y;
						successful = true;
					}

				} else {
					int currentDiff = 0;
					while (!level().noCollision(this, getBoundingBox().move(Vec3.atBottomCenterOf(startPos).subtract(position())))
						&& currentDiff < 15) {
						startPos = startPos.above();
						currentDiff++;
					}

					if (level().noCollision(this, getBoundingBox().move(Vec3.atBottomCenterOf(startPos).subtract(position())))) {
						BlockHitResult result = level().clip(new ClipContext(
							Vec3.atCenterOf(startPos),
							Vec3.atCenterOf(startPos).add(0, -15, 0),
							ClipContext.Block.COLLIDER,
							ClipContext.Fluid.NONE,
							this
						));

						if (result.getType() != HitResult.Type.MISS) {
							finalY = result.getLocation().y;
							successful = true;
						}
					}
				}

				Vec3 target = new Vec3(startPos.getX() + 0.5, finalY, startPos.getZ() + 0.5);

				if (successful && initialPos.distanceTo(target) <= 15) {
					if (ESPlatform.INSTANCE.postTeleportEvent(this, target)) {
						setPos(target);
						break;
					}
				}
			}
		}
	}

	public boolean isGatekeeperStuck() {
		if (recentPositions.size() >= 32) {
			float averageX = 0;
			float averageY = 0;
			float averageZ = 0;
			for (BlockPos pos : recentPositions) {
				averageX += pos.getX();
				averageY += pos.getY();
				averageZ += pos.getZ();
			}
			averageX = averageX / recentPositions.size();
			averageY = averageY / recentPositions.size();
			averageZ = averageZ / recentPositions.size();
			Vec3 averagePos = new Vec3(averageX, averageY, averageZ);
			return recentPositions.stream().allMatch(pos ->
				Vec3.atBottomCenterOf(pos).distanceTo(averagePos) < 5);
		}
		return false;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		bossEvent.update();
		if (tickCount % 5 == 0 && !isActivated()) {
			bossEvent.allConvertToUnseen();
		}
		refreshDimensions();
		if (!level().isClientSide) {
			if (standardFight) {
				LivingEntity target = getTarget();
				if ((!isAlive() || !isActivated()) || (target != null && target.isAlive())) {
					noTargetTime = 0;
				}
				if (tickCount > 5 && isAlive() && isActivated() && (target == null || !target.isAlive())) {
					noTargetTime++;
					if (getFightTarget().isEmpty() || noTargetTime > 200) {
						abortFight();
						noTargetTime = 0;
					}
				}
			} else {
				if (!isActivated()) {
					setActivated(true);
				}
			}
			if (restockCooldown > 0) {
				restockCooldown--;
			} else {
				restockCooldown = 12000;
				restockAll();
			}
			if (getTradingPlayer() != null && getTradingPlayer().distanceTo(this) > 16) {
				setTradingPlayer(null);
			}
			setCustomName(Component.literal(gatekeeperName));
			setCustomNameVisible(true);
			if (isLeftHanded()) {
				setLeftHanded(false);
			}
			if (isActivated() && !isNoAi() && isAlive()) {
				behaviorManager.tick();
				if (tickCount % 10 == 0) {
					recentPositions.add(blockPosition());
				}
				while (recentPositions.size() > 32) {
					recentPositions.remove(0);
				}
			} else {
				setBehaviorState(0);
				setBehaviorTicks(0);
				recentPositions.clear();
			}
			if (!isActivated() || getBehaviorState() != 0) {
				getNavigation().stop();
				getMoveControl().setWantedPosition(getX(), getY(), getZ(), 0);
			}
			if (!isActivated()) {
				boolean blockSupport = false;
				for (Direction direction : Direction.values()) {
					if (direction.getAxis() != Direction.Axis.Y) {
						BlockHitResult toSide = level().clip(new ClipContext(position().add(0, getBbHeight(), 0), position().add(0, getBbHeight(), 0).add(new Vec3(direction.step()).scale(getBbWidth() / 2 + 0.25)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
						if (toSide.getType() != HitResult.Type.MISS) {
							setStandDirection(direction.getOpposite());
							blockSupport = true;
						}
					}
				}
				AABB box = super.getDimensions(getPose()).makeBoundingBox(position());
				if (!blockSupport || !level().noCollision(this, box)) {
				}
			} else {
				setStandDirection(Direction.UP);
			}
		} else {
			if (isActivated()) {
				sitAnimationState.stop();
				standAnimationState.stop();
			} else {
				if (getStandDirection().getAxis() != Direction.Axis.Y) {
					sitAnimationState.stop();
					standAnimationState.startIfStopped(tickCount);
				} else {
					sitAnimationState.startIfStopped(tickCount);
					standAnimationState.stop();
				}
			}
		}
	}

	public ItemStack getGatekeeperHammer() {
		LivingEntity target = getTarget();
		if (target instanceof ServerPlayer serverPlayer && isPlayerPermitted(serverPlayer) && ESDataAttachments.BOSS_CHALLENGE_COUNT.getData(target).getOrDefault(EntityType.getKey(getType()), 0) > 0) {
			ItemStack weapon = Items.NETHERITE_AXE.getDefaultInstance();
			ItemStack source = ESItems.GLISTERING_MORNING_STAR.get().getDefaultInstance();
			Multimap<Attribute, AttributeModifier> mods = source.getAttributeModifiers(EquipmentSlot.MAINHAND);

			for (Map.Entry<Attribute, AttributeModifier> entry : mods.entries()) {
				weapon.addAttributeModifier(entry.getKey(), entry.getValue(), EquipmentSlot.MAINHAND);
			}

			return weapon;
		}

		return ESItems.GLISTERING_MORNING_STAR.get().getDefaultInstance();
	}

	public void abortFight() {
		setHealth(getMaxHealth());
		setFightTargetName("");
		setTarget(null);
		setActivated(false);
		tryTeleportBack();
		fightParticipants.clear();
		healCount = 0;
		healInterruptedCount = 0;
		setItemInHand(InteractionHand.MAIN_HAND, ESItems.GLISTERING_SWORD.get().getDefaultInstance());
		setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		EntityDimensions dimensions = super.getDimensions(pose);
		if (!isActivated() && getStandDirection().getAxis() == Direction.Axis.Y) {
			return dimensions.scale(1, 0.7f);
		}
		return dimensions;
	}

	@Override
	protected void grantSpecialLoot(ServerPlayer player) {
		permitPlayer(player);
		ESCrestUtil.upgradeCrest(player, ESCrests.GUIDANCE_OF_STARS);
	}

	@Override
	public float getStandingEyeHeight(Pose pose, EntityDimensions size) {
		return 1.74F;
	}

	@Override
	protected void modifyBossLootChest(LootChestBlockEntity blockEntity) {
		blockEntity.setColor(0x424c6e);
		blockEntity.setRareFlashColor(0x657392);
	}

	@Override
	public SoundEvent getBossMusic() {
		return ESSoundEvents.MUSIC_BOSS_GATEKEEPER.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.PLAYER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.PLAYER_DEATH;
	}

	@Override
	public void setTradingPlayer(@Nullable Player player) {
		this.customer = player;
	}

	@Nullable
	@Override
	public Player getTradingPlayer() {
		return this.customer;
	}

	@Override
	public MerchantOffers getOffers() {
		if (this.offers == null) {
			this.offers = new MerchantOffers();
			this.addTrades();
		}

		return this.offers;
	}

	protected void addTrades() {
		MerchantOffers offers = this.getOffers();

		this.addTrades(offers, GatekeeperTrades.TRADES, 50);

		this.addTrades(offers, new VillagerTrades.ItemListing[]{
			ESEntityUtil.simpleTrade(
				new ItemStack(ESItems.STARLIGHT_SILVER_COIN.get(), 10),
				makeGuardianPainting(),
				10
			)
		}, 50);
	}

	private ItemStack makeGuardianPainting() {
		ItemStack stack = ESItems.STARLIT_PAINTING.get().getDefaultInstance();

		CompoundTag entityTag = stack.getOrCreateTagElement("EntityTag");
		entityTag.putString("variant", ESPaintingVariants.GUARDIAN.location().toString());

		return stack;
	}

	protected void addTrades(MerchantOffers original, VillagerTrades.ItemListing[] newTrades, int maxNumbers) {
		Set<Integer> set = new HashSet<>();
		if (newTrades.length > maxNumbers) {
			while (set.size() < maxNumbers) {
				set.add(this.random.nextInt(newTrades.length));
			}
		} else {
			for (int i = 0; i < newTrades.length; ++i) {
				set.add(i);
			}
		}

		for (Integer integer : set) {
			VillagerTrades.ItemListing trade = newTrades[integer];
			MerchantOffer merchantoffer = trade.getOffer(this, this.random);
			if (merchantoffer != null) {
				original.add(merchantoffer);
			}
		}
	}

	protected void restockAll() {
		for (MerchantOffer merchantoffer : this.getOffers()) {
			merchantoffer.resetUses();
		}
	}

	@Override
	public void overrideOffers(@Nullable MerchantOffers offers) {

	}

	@Override
	public void notifyTrade(MerchantOffer offer) {
		offer.increaseUses();
		this.ambientSoundTime = -this.getAmbientSoundInterval();
		if (offer.shouldRewardExp()) {
			int i = 3 + this.random.nextInt(4);
			this.level().addFreshEntity(new ExperienceOrb(this.level(), this.getX(), this.getY() + 0.5D, this.getZ(), i));
		}
	}

	@Override
	public void notifyTradeUpdated(ItemStack stack) {

	}

	@Override
	public int getVillagerXp() {
		return 0;
	}

	@Override
	public void overrideXp(int xp) {

	}

	@Override
	public boolean showProgressBar() {
		return false;
	}

	@Override
	public SoundEvent getNotifyTradeSound() {
		return SoundEvents.PLAYER_LEVELUP;
	}

	@Override
	public boolean isClientSide() {
		return this.level().isClientSide;
	}
}
