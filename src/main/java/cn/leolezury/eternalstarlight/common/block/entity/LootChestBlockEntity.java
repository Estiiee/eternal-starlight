package cn.leolezury.eternalstarlight.common.block.entity;

import cn.leolezury.eternalstarlight.common.network.ParticlePacket;
import cn.leolezury.eternalstarlight.common.particle.ExplosionShockParticleOptions;
import cn.leolezury.eternalstarlight.common.platform.ESPlatform;
import cn.leolezury.eternalstarlight.common.registry.ESBlockEntities;
import cn.leolezury.eternalstarlight.common.registry.ESDataAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class LootChestBlockEntity extends BlockEntity {
	private static final String TAG_LOOT_TABLE = "loot_table";
	private static final String TAG_ITEMS_TO_EJECT = "items_to_eject";
	private static final String TAG_QUICK_EJECTION = "quick_ejection";
	private static final String TAG_REWARD_TARGETS = "reward_targets";
	private static final String TAG_CURRENT_REWARD_TARGET = "current_reward_target";
	private static final String TAG_EJECTION_TICKS = "ejection_ticks";
	private static final String TAG_COOLDOWN = "cooldown";
	private static final String TAG_COLOR = "color";
	private static final String TAG_OUTLINE_COLOR = "outline_color";
	private static final String TAG_FLASH_COLOR = "flash_color";
	private static final String TAG_RARE_FLASH_COLOR = "rare_flash_color";

	private ResourceLocation lootTable;
	private final List<ItemStack> itemsToEject = new ArrayList<>();
	private boolean quickEjection = false;
	private final List<UUID> rewardTargets = new ArrayList<>();
	private UUID currentRewardTarget;
	private int ejectionTicks, cooldown;
	private int color = -1, outlineColor = -1, flashColor = -1, rareFlashColor = -1;

	public AnimationState openAnimationState = new AnimationState();
	public AnimationState closeAnimationState = new AnimationState();
	public int clientTickCount = 0;
	public int flashStartTickCount = Integer.MIN_VALUE;
	public boolean rareFlash = false;

	public void setLootTable(ResourceLocation lootTable) {
		this.lootTable = lootTable;
		setChanged();
	}

	public List<UUID> getRewardTargets() {
		return Collections.unmodifiableList(rewardTargets);
	}

	public void addRewardTarget(UUID rewardTarget) {
		this.rewardTargets.add(rewardTarget);
		setChanged();
	}

	public void rewardPlayer(ServerPlayer player, BlockPos pos, Block block, boolean quickEjection) {
		player.level().blockEvent(pos, block, 1, 0);
		this.quickEjection = quickEjection;
		this.currentRewardTarget = player.getUUID();
		this.rewardTargets.remove(player.getUUID());
		if (this.lootTable != null) {
			itemsToEject.clear();
			ServerLevel serverLevel = player.serverLevel();
			MinecraftServer server = serverLevel.getServer();
			LootTable table = server.getLootData().getLootTable(this.lootTable);
			LootParams.Builder paramBuilder = new LootParams.Builder(serverLevel).withParameter(LootContextParams.THIS_ENTITY, player).withParameter(LootContextParams.ORIGIN, player.position()).withParameter(LootContextParams.DAMAGE_SOURCE, this.level.damageSources().generic());
			LootParams params = paramBuilder.create(LootContextParamSets.ENTITY);
			itemsToEject.addAll(table.getRandomItems(params));
		}
		setChanged();
	}

	public boolean isFree() {
		return this.currentRewardTarget == null && this.cooldown <= 0;
	}

	public boolean isEjecting() {
		return ejectionTicks > 0;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
		setChanged();
		if (getLevel() != null) {
			getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
		}
	}

	public int getOutlineColor() {
		return outlineColor;
	}

	public void setOutlineColor(int outlineColor) {
		this.outlineColor = outlineColor;
		setChanged();
		if (getLevel() != null) {
			getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
		}
	}

	public int getFlashColor() {
		return flashColor;
	}

	public void setFlashColor(int flashColor) {
		this.flashColor = flashColor;
		setChanged();
		if (getLevel() != null) {
			getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
		}
	}

	public int getRareFlashColor() {
		return rareFlashColor;
	}

	public void setRareFlashColor(int rareFlashColor) {
		this.rareFlashColor = rareFlashColor;
		setChanged();
		if (getLevel() != null) {
			getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
		}
	}

	public LootChestBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(ESBlockEntities.LOOT_CHEST.get(), blockPos, blockState);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, LootChestBlockEntity blockEntity) {
		if (!level.isClientSide) {
			if (blockEntity.currentRewardTarget != null) {
				blockEntity.ejectionTicks++;
				if (blockEntity.ejectionTicks == 1) {
					level.sendBlockUpdated(pos, state, state, 3);
				}
				if (blockEntity.ejectionTicks % 10 == 0) {
					blockEntity.setChanged();
				}
				if ((blockEntity.ejectionTicks + 5) % 10 == 0 && !blockEntity.itemsToEject.isEmpty()) {
					boolean rare = blockEntity.itemsToEject.get(0).getRarity() != Rarity.COMMON && !blockEntity.quickEjection;
					level.blockEvent(pos, state.getBlock(), 2, rare ? 1 : 0);
				}
				if (blockEntity.ejectionTicks % 10 == 0) {
					if (blockEntity.itemsToEject.isEmpty()) {
						if (blockEntity.ejectionTicks > 20) {
							blockEntity.currentRewardTarget = null;
							blockEntity.cooldown = 20;
							blockEntity.ejectionTicks = 0;
							blockEntity.setChanged();
							level.sendBlockUpdated(pos, state, state, 3);
							level.blockEvent(pos, state.getBlock(), 1, 1);
							level.playSound(null, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 1.0F, 1.0F);
						}
					} else {
						if (blockEntity.quickEjection) {
							for (ItemStack stack : blockEntity.itemsToEject) {
								ejectItem(level, pos, blockEntity, stack);
							}
							blockEntity.itemsToEject.clear();
						} else {
							ItemStack stack = blockEntity.itemsToEject.remove(0);
							ejectItem(level, pos, blockEntity, stack);
						}
						blockEntity.setChanged();
					}
				}
			}
			if (blockEntity.cooldown > 0) {
				blockEntity.cooldown--;
				if (blockEntity.cooldown % 10 == 0) {
					blockEntity.setChanged();
				}
			}
			if (blockEntity.isFree() && blockEntity.getRewardTargets().isEmpty() && level instanceof ServerLevel serverLevel) {
				for (int i = 0; i < 15; i++) {
					Vec3 speed = new Vec3((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.1F, level.getRandom().nextFloat() * 0.05F, (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.1F).normalize();
					ESPlatform.INSTANCE.sendToAllClients(serverLevel, new ParticlePacket(ExplosionShockParticleOptions.fromIntColor(new Vector3f(FastColor.ARGB32.red(blockEntity.getColor()), FastColor.ARGB32.green(blockEntity.getColor()), FastColor.ARGB32.blue(blockEntity.getColor())), new Vector3f(FastColor.ARGB32.red(blockEntity.getOutlineColor()), FastColor.ARGB32.green(blockEntity.getOutlineColor()), FastColor.ARGB32.blue(blockEntity.getOutlineColor())), 0.8f, 0.04f, 0.8f), pos.getX() + 0.5 + speed.x * 0.2, pos.getY() + 0.5 + speed.y * 0.2, pos.getZ() + 0.5 + speed.z * 0.2, speed.x, speed.y, speed.z));
				}
				level.destroyBlock(pos, false);
			}
		} else {
			blockEntity.clientTickCount++;
		}
	}

	public static void ejectItem(Level level, BlockPos pos, LootChestBlockEntity blockEntity, ItemStack stack) {
		Vec3 position = Vec3.atBottomCenterOf(pos).relative(Direction.UP, 0.625);
		double x = position.x();
		double y = position.y();
		double z = position.z();
		ItemEntity itemEntity = new ItemEntity(level, x, y, z, stack);
		itemEntity.setDeltaMovement(
			level.random.triangle(0, 0.035),
			level.random.triangle(0.2, 0.035),
			level.random.triangle(0, 0.035)
		);
		level.addFreshEntity(itemEntity);
		ESDataAttachments.IMPORTANT_ITEM.setData(itemEntity, true);
		itemEntity.setTarget(blockEntity.currentRewardTarget);
		itemEntity.setGlowingTag(true);
		itemEntity.setExtendedLifetime();
	}

	@Override
	public boolean triggerEvent(int id, int type) {
		if (id == 1) {
			openAnimationState.stop();
			closeAnimationState.stop();
			if (type == 0) {
				openAnimationState.startIfStopped(clientTickCount);
			} else if (type == 1) {
				closeAnimationState.startIfStopped(clientTickCount);
			}
			return true;
		} else if (id == 2) {
			flashStartTickCount = clientTickCount;
			if (type == 0) {
				rareFlash = false;
			} else if (type == 1) {
				rareFlash = true;
			}
			return true;
		} else {
			return super.triggerEvent(id, type);
		}
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return saveWithFullMetadata();
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);

		if (tag.contains(TAG_LOOT_TABLE, Tag.TAG_STRING)) {
			this.lootTable = new ResourceLocation(tag.getString(TAG_LOOT_TABLE));
		}

		if (tag.contains(TAG_ITEMS_TO_EJECT, Tag.TAG_LIST)) {
			ListTag list = tag.getList(TAG_ITEMS_TO_EJECT, Tag.TAG_COMPOUND);
			this.itemsToEject.clear();
			for (Tag t : list) {
				this.itemsToEject.add(ItemStack.of((CompoundTag) t));
			}
		}

		this.quickEjection = tag.getBoolean(TAG_QUICK_EJECTION);

		if (tag.contains(TAG_REWARD_TARGETS, Tag.TAG_LIST)) {
			ListTag list = tag.getList(TAG_REWARD_TARGETS, Tag.TAG_INT_ARRAY);
			this.rewardTargets.clear();
			for (Tag t : list) {
				this.rewardTargets.add(NbtUtils.loadUUID(t));
			}
		}

		if (tag.contains(TAG_CURRENT_REWARD_TARGET)) {
			this.currentRewardTarget = tag.getUUID(TAG_CURRENT_REWARD_TARGET);
		}

		this.cooldown = tag.getInt(TAG_COOLDOWN);
		this.ejectionTicks = tag.getInt(TAG_EJECTION_TICKS);

		if (tag.contains(TAG_COLOR)) this.color = tag.getInt(TAG_COLOR);
		if (tag.contains(TAG_OUTLINE_COLOR)) this.outlineColor = tag.getInt(TAG_OUTLINE_COLOR);
		if (tag.contains(TAG_FLASH_COLOR)) this.flashColor = tag.getInt(TAG_FLASH_COLOR);
		if (tag.contains(TAG_RARE_FLASH_COLOR)) this.rareFlashColor = tag.getInt(TAG_RARE_FLASH_COLOR);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);

		if (this.lootTable != null) {
			tag.putString(TAG_LOOT_TABLE, this.lootTable.toString());
		}

		ListTag itemsList = new ListTag();
		for (ItemStack stack : this.itemsToEject) {
			itemsList.add(stack.save(new CompoundTag()));
		}
		tag.put(TAG_ITEMS_TO_EJECT, itemsList);

		tag.putBoolean(TAG_QUICK_EJECTION, this.quickEjection);

		ListTag uuidList = new ListTag();
		for (UUID id : this.rewardTargets) {
			uuidList.add(NbtUtils.createUUID(id));
		}
		tag.put(TAG_REWARD_TARGETS, uuidList);

		if (this.currentRewardTarget != null) {
			tag.putUUID(TAG_CURRENT_REWARD_TARGET, this.currentRewardTarget);
		}

		tag.putInt(TAG_COOLDOWN, this.cooldown);
		tag.putInt(TAG_EJECTION_TICKS, this.ejectionTicks);
		tag.putInt(TAG_COLOR, this.color);
		tag.putInt(TAG_OUTLINE_COLOR, this.outlineColor);
		tag.putInt(TAG_FLASH_COLOR, this.flashColor);
		tag.putInt(TAG_RARE_FLASH_COLOR, this.rareFlashColor);
	}

}
