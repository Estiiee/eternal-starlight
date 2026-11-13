package cn.leolezury.eternalstarlight.common.entity.misc;

import cn.leolezury.eternalstarlight.common.registry.ESEntities;
import cn.leolezury.eternalstarlight.common.registry.ESItems;
import cn.leolezury.eternalstarlight.common.util.ESTags;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ESPainting extends Painting {
	private static final String TAG_ITEM = "item";

	private static final EntityDataAccessor<ItemStack> ITEM_STACK = SynchedEntityData.defineId(ESPainting.class, EntityDataSerializers.ITEM_STACK);

	public ESPainting(Level level, BlockPos blockPos) {
		super(ESEntities.PAINTING.get(), level);
		this.pos = blockPos;
		this.direction = Direction.SOUTH;
	}

	public ESPainting(EntityType<? extends ESPainting> type, Level level) {
		super(type, level);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(ITEM_STACK, ESItems.STARLIT_PAINTING.get().getDefaultInstance());
	}

	public static Optional<ESPainting> createPainting(Level level, ItemStack item, BlockPos blockPos, Direction direction) {
		ESPainting painting = new ESPainting(level, blockPos);

		// Get CT from the painting to look for specific data.
		CompoundTag entityTag = item.getTagElement("EntityTag");

		if (entityTag != null && entityTag.contains("variant", CompoundTag.TAG_STRING)) {

			// Retrieve the variantId if present.
			ResourceLocation variantId = ResourceLocation.tryParse(
				entityTag.getString("variant")
			);

			if (variantId == null) {
				return Optional.empty();
			}

			var registry = level.registryAccess()
				.registryOrThrow(Registries.PAINTING_VARIANT);

			var variant = registry.getHolder(
				net.minecraft.resources.ResourceKey.create(
					Registries.PAINTING_VARIANT,
					variantId
				)
			);

			if (variant.isEmpty()) {
				return Optional.empty();
			}

			// If valid variant, place it.

			painting.setVariant(variant.get());
			painting.setDirection(direction);

			if (!painting.survives()) {
				return Optional.empty();
			}

			painting.setItem(item);
			return Optional.of(painting);
		}

		List<Holder<PaintingVariant>> list = new ArrayList<>();
		Iterable<Holder<PaintingVariant>> placeable = level.registryAccess().registryOrThrow(Registries.PAINTING_VARIANT).getTagOrEmpty(ESTags.PaintingVariants.PLACEABLE);
		placeable.forEach(list::add);
		if (list.isEmpty()) {
			return Optional.empty();
		} else {
			painting.setDirection(direction);
			list.removeIf((holder) -> {
				painting.setVariant(holder);
				return !painting.survives();
			});
			if (list.isEmpty()) {
				return Optional.empty();
			} else {
				int i = list.stream()
					.mapToInt(holder -> holder.value().getWidth() * holder.value().getHeight())
					.max()
					.orElse(0);

				list.removeIf(holder -> holder.value().getWidth() * holder.value().getHeight() < i);

				Optional<Holder<PaintingVariant>> optional = Util.getRandomSafe(list, painting.level().getRandom());
				if (optional.isEmpty()) {
					return Optional.empty();
				} else {
					painting.setVariant(optional.get());
					painting.setDirection(direction);
					painting.setItem(item);
					return Optional.of(painting);
				}
			}
		}
	}

	public void setItem(ItemStack item) {
		this.getEntityData().set(ITEM_STACK, item.copy());
	}

	public ItemStack getItem() {
		return this.getEntityData().get(ITEM_STACK);
	}

	@Override
	public void dropItem(@Nullable Entity entity) {
		if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
			this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
			if (entity instanceof Player player) {
				if (player.getAbilities().instabuild) {
					return;
				}
			}

			this.spawnAtLocation(getItem());
		}
	}

	@Override
	public ItemStack getPickResult() {
		return getItem().copy();
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.put(TAG_ITEM, this.getItem().save(new CompoundTag()));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);

		if (compoundTag.contains(TAG_ITEM, CompoundTag.TAG_COMPOUND)) {
			this.setItem(ItemStack.of(compoundTag.getCompound(TAG_ITEM)));
		} else {
			this.setItem(ESItems.STARLIT_PAINTING.get().getDefaultInstance());
		}
	}
}
