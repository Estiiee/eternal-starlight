package cn.leolezury.eternalstarlight.common.entity.living.boss;

import cn.leolezury.eternalstarlight.common.block.LootChestBlock;
import cn.leolezury.eternalstarlight.common.block.entity.LootChestBlockEntity;
import cn.leolezury.eternalstarlight.common.block.entity.spawner.BossSpawnerBlockEntity;
import cn.leolezury.eternalstarlight.common.config.ESConfig;
import cn.leolezury.eternalstarlight.common.entity.living.phase.MultiBehaviorUser;
import cn.leolezury.eternalstarlight.common.item.misc.LootBagItem;
import cn.leolezury.eternalstarlight.common.platform.ESPlatform;
import cn.leolezury.eternalstarlight.common.registry.*;
import cn.leolezury.eternalstarlight.common.util.GlobalVec3;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class ESBoss extends Monster implements MultiBehaviorUser {
	private static final String TAG_INITIAL_POS = "initial_pos";
	private static final String TAG_SPAWNED = "spawned";
	private static final String TAG_PHASE = "phase";
	private static final String TAG_ACTIVATED = "activated";

	protected final List<UUID> fightParticipants = new ArrayList<>();

	protected ESBoss(EntityType<? extends ESBoss> type, Level level) {
		super(type, level);
	}

	protected static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(ESBoss.class, EntityDataSerializers.INT);

	public int getPhase() {
		return this.getEntityData().get(PHASE);
	}

	public void setPhase(int phase) {
		this.getEntityData().set(PHASE, phase);
	}

	protected static final EntityDataAccessor<Integer> BEHAVIOR_STATE = SynchedEntityData.defineId(ESBoss.class, EntityDataSerializers.INT);

	public int getBehaviorState() {
		return this.getEntityData().get(BEHAVIOR_STATE);
	}

	public void setBehaviorState(int behaviourState) {
		this.getEntityData().set(BEHAVIOR_STATE, behaviourState);
	}

	protected static final EntityDataAccessor<Integer> BEHAVIOR_TICKS = SynchedEntityData.defineId(ESBoss.class, EntityDataSerializers.INT);

	public int getBehaviorTicks() {
		return this.getEntityData().get(BEHAVIOR_TICKS);
	}

	public void setBehaviorTicks(int behaviourTicks) {
		this.getEntityData().set(BEHAVIOR_TICKS, behaviourTicks);
	}

	protected static final EntityDataAccessor<Boolean> ACTIVATED = SynchedEntityData.defineId(ESBoss.class, EntityDataSerializers.BOOLEAN);

	public boolean isActivated() {
		return this.getEntityData().get(ACTIVATED);
	}

	public void setActivated(boolean activated) {
		this.getEntityData().set(ACTIVATED, activated);
		if (activated) {
			setBehaviorState(0);
			setBehaviorTicks(0);
		}
	}

	private GlobalVec3 initialPos = GlobalVec3.of(Level.OVERWORLD, Vec3.ZERO);
	private boolean spawned = false;

	public GlobalVec3 getInitialPos() {
		return initialPos;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(BEHAVIOR_STATE, 0);
		this.entityData.define(BEHAVIOR_TICKS, 0);
		this.entityData.define(PHASE, 0);
		this.entityData.define(ACTIVATED, true);
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
		spawnData = super.finalizeSpawn(level, difficulty, spawnType, spawnData, dataTag);
		initializeBossOnFirstSpawn();
		return spawnData;
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);

		if (tag.contains(TAG_INITIAL_POS, CompoundTag.TAG_COMPOUND)) {
			CompoundTag posTag = tag.getCompound(TAG_INITIAL_POS);

			ResourceLocation dimId = new ResourceLocation(posTag.getString("dim"));
			ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, dimId);

			double x = posTag.getDouble("x");
			double y = posTag.getDouble("y");
			double z = posTag.getDouble("z");

			this.initialPos = new GlobalVec3(dim, new Vec3(x, y, z));
		}

		spawned = tag.getBoolean(TAG_SPAWNED);
		setPhase(tag.getInt(TAG_PHASE));
		setActivated(tag.getBoolean(TAG_ACTIVATED));
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);

		CompoundTag posTag = new CompoundTag();
		posTag.putString("dim", initialPos.dimension().location().toString());
		posTag.putDouble("x", initialPos.pos().x());
		posTag.putDouble("y", initialPos.pos().y());
		posTag.putDouble("z", initialPos.pos().z());
		tag.put(TAG_INITIAL_POS, posTag);

		tag.putBoolean(TAG_SPAWNED, spawned);
		tag.putInt(TAG_PHASE, getPhase());
		tag.putBoolean(TAG_ACTIVATED, isActivated());
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (source.is(DamageTypes.IN_WALL) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			return false;
		}
		boolean success = super.hurt(source, amount);
		if (success && source.getEntity() instanceof ServerPlayer player && !fightParticipants.contains(player.getUUID())) {
			fightParticipants.add(player.getUUID());
		}
		return success;
	}

	@Override
	public void die(DamageSource source) {
		if (source.getEntity() instanceof ServerPlayer player && !fightParticipants.contains(player.getUUID())) {
			fightParticipants.add(player.getUUID());
		}
		if (!level().isClientSide) {
			for (UUID uuid : fightParticipants) {
				Player player = level().getPlayerByUUID(uuid);
				if (player instanceof ServerPlayer serverPlayer && player.isAlive() && player.level().dimension() == level().dimension()) {
					CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(serverPlayer, this, source);
				}
			}
		}
		super.die(source);
	}

	@Override
	public boolean removeWhenFarAway(double dist) {
		return false;
	}

	@Override
	public boolean canChangeDimensions() {
		return false;
	}

	@Override
	public boolean startRiding(Entity entity, boolean bl) {
		return false;
	}

	public boolean canBossMove() {
		return true;
	}

	public void initializeBoss() {
		initialPos = GlobalVec3.of(level().dimension(), position());
	}

	public void initializeBossOnFirstSpawn() {
		if (!spawned) {
			initializeBoss();
			spawned = true;
		}
	}

	public boolean shouldPlayBossMusic() {
		return isAlive() && getBossMusic() != null;
	}

	public SoundEvent getBossMusic() {
		return ESSoundEvents.MUSIC_BOSS_GATEKEEPER.get();
	}

	public ResourceLocation getBossLootTable() {
		ResourceLocation base = BuiltInRegistries.ENTITY_TYPE.getKey(getType());
		return new ResourceLocation(base.getNamespace(), "bosses/" + base.getPath());
	}

	public ItemStack getBossLootBag() {
		ItemStack lootBag = new ItemStack(ESItems.LOOT_BAG.get());
		LootBagItem.setLootTable(lootBag, getBossLootTable());
		return lootBag;
	}

	@Override
	public void remove(RemovalReason reason) {
		if (reason == RemovalReason.KILLED) {
			bossDefeat();
		}
		super.remove(reason);
	}

	protected void increaseBossChallengeCount(Player player) {
		Map<ResourceLocation, Integer> challengeCount = ESDataAttachments.BOSS_CHALLENGE_COUNT.getData(player);
		Map<ResourceLocation, Integer> newChallengeCount = new HashMap<>(challengeCount);
		newChallengeCount.compute(EntityType.getKey(getType()), (k, v) -> v == null ? 1 : v + 1);
		ESDataAttachments.BOSS_CHALLENGE_COUNT.setData(player, newChallengeCount);
	}

	protected void bossDefeat() {
		trySpawnLoot();
		for (UUID uuid : fightParticipants) {
			Player player = level().getPlayerByUUID(uuid);
			if (player != null && player.isAlive() && player.level().dimension() == level().dimension()) {
				increaseBossChallengeCount(player);
			}
		}
		if (ESConfig.enableBossRespawn.get()) {
			BlockState spawnerState = getBossSpawnerForRespawn();
			if (!spawnerState.isAir() && initialPos.dimension() == level().dimension()) {
				BlockPos spawnerPos = BlockPos.containing(initialPos.pos());
				if (canBossSpawnerReplace(spawnerPos, level().getBlockState(spawnerPos))) {
					level().setBlockAndUpdate(spawnerPos, spawnerState);
					if (level().getBlockEntity(spawnerPos) instanceof BossSpawnerBlockEntity<?> blockEntity) {
						blockEntity.setSpawnCooldown(ESConfig.bossRespawnCooldown.get());
					}
				}
			}
		}
		fightParticipants.clear();
	}

	protected BlockState getBossSpawnerForRespawn() {
		return Blocks.AIR.defaultBlockState();
	}

	protected boolean shouldSpawnLoot() {
		return level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT);
	}

	protected boolean shouldSpawnLootChest() {
		return ESConfig.enableLootChest.get() && !fightParticipants.isEmpty();
	}

	protected void trySpawnLoot() {
		if (level() instanceof ServerLevel serverLevel && shouldSpawnLoot()) {
			if (!spawnBossLootChest(serverLevel)) {
				ItemStack lootBag = getBossLootBag();
				if (fightParticipants.stream().noneMatch(uuid -> level().getPlayerByUUID(uuid) != null)) {
					ItemEntity item = spawnAtLocation(lootBag.copy());
					if (item != null) {
						ESDataAttachments.IMPORTANT_ITEM.setData(item, true);
						item.setGlowingTag(true);
						item.setExtendedLifetime();
					}
				}
				for (UUID uuid : fightParticipants) {
					Player player = level().getPlayerByUUID(uuid);
					if (player != null && player.isAlive() && player.level().dimension() == level().dimension()) {
						ItemEntity item = player.spawnAtLocation(lootBag.copy());
						if (item != null) {
							ESDataAttachments.IMPORTANT_ITEM.setData(item, true);
							item.setTarget(player.getUUID());
							item.setGlowingTag(true);
							item.setExtendedLifetime();
						}
					}
				}
			}
		}
		for (UUID uuid : fightParticipants) {
			Player player = level().getPlayerByUUID(uuid);
			if (player instanceof ServerPlayer serverPlayer && player.isAlive() && player.level().dimension() == level().dimension()) {
				grantSpecialLoot(serverPlayer);
			}
		}
	}

	protected void grantSpecialLoot(ServerPlayer player) {
	}

	protected boolean canBossSpawnerReplace(BlockPos pos, BlockState state) {
		return state.isAir() || (state.canBeReplaced() && ESPlatform.INSTANCE.postEntityDestroyBlockEvent(level(), pos, this));
	}

	protected boolean canBossLootChestReplace(BlockPos pos, BlockState state) {
		return state.isAir() || (state.canBeReplaced() && ESPlatform.INSTANCE.postEntityDestroyBlockEvent(level(), pos, this));
	}

	protected Optional<BlockPos> getLootChestPos() {
		BlockPos chestPos = blockPosition();
		while (canBossLootChestReplace(chestPos, level().getBlockState(chestPos)) && chestPos.getY() > level().getMinBuildHeight()) {
			chestPos = chestPos.below();
		}
		chestPos = chestPos.above();
		if (shouldSpawnLootChest() && canBossLootChestReplace(chestPos, level().getBlockState(chestPos))) {
			return Optional.of(chestPos);
		}
		return Optional.empty();
	}

	protected boolean spawnBossLootChest(ServerLevel serverLevel) {
		Optional<BlockPos> possibleChestPos = getLootChestPos();
		if (possibleChestPos.isPresent()) {
			BlockPos chestPos = possibleChestPos.get();
			serverLevel.setBlockAndUpdate(chestPos, ESBlocks.LOOT_CHEST.get().defaultBlockState().setValue(LootChestBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(serverLevel.getRandom())));
			if (serverLevel.getBlockEntity(chestPos) instanceof LootChestBlockEntity blockEntity) {
				blockEntity.setLootTable(getBossLootTable());
				for (UUID uuid : fightParticipants) {
					blockEntity.addRewardTarget(uuid);
				}
				modifyBossLootChest(blockEntity);
				return true;
			}
		}
		return false;
	}

	protected void modifyBossLootChest(LootChestBlockEntity blockEntity) {
	}

	@Override
	public void tick() {
		super.tick();
		if (!level().isClientSide) {
			initializeBossOnFirstSpawn();
			if (!canBossMove() && level().dimension() == initialPos.dimension()) {
				setPos(initialPos.pos().x, position().y, initialPos.pos().z);
			}
			fightParticipants.removeIf(uuid -> {
				Player player = level().getPlayerByUUID(uuid);
				return player == null || !player.isAlive();
			});
		}
	}
}
