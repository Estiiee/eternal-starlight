package cn.leolezury.eternalstarlight.common.item.misc;

import cn.leolezury.eternalstarlight.common.item.loot.ESLootContextParamSets;
import cn.leolezury.eternalstarlight.common.item.loot.ESLootContextParams;
import cn.leolezury.eternalstarlight.common.registry.ESDataAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.HashMap;
import java.util.Map;

public class LootBagItem extends Item {
	public LootBagItem(Properties properties) {
		super(properties);
	}

	public static ResourceLocation getLootTable(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag == null || !tag.contains("LootTable")) return null;
		return new ResourceLocation(tag.getString("LootTable"));
	}

	public static void setLootTable(ItemStack stack, ResourceLocation table) {
		stack.getOrCreateTag().putString("LootTable", table.toString());
	}

	public static void setBossChallengeCounts(ItemStack stack, Map<ResourceLocation, Integer> challengeCounts) {
		CompoundTag countsTag = new CompoundTag();
		for (Map.Entry<ResourceLocation, Integer> entry : challengeCounts.entrySet()) {
			countsTag.putInt(entry.getKey().toString(), entry.getValue());
		}
		stack.getOrCreateTag().put("BossChallengeCounts", countsTag);
	}

	public static Map<ResourceLocation, Integer> getBossChallengeCounts(ItemStack stack) {
		Map<ResourceLocation, Integer> result = new HashMap<>();
		if (!stack.hasTag()) return result;
		CompoundTag tag = stack.getTag();
		if (!tag.contains("BossChallengeCounts")) return result;

		CompoundTag countsTag = tag.getCompound("BossChallengeCounts");
		for (String key : countsTag.getAllKeys()) {
			result.put(new ResourceLocation(key), countsTag.getInt(key));
		}
		return result;
	}

	private boolean dropLoot(Level level, Player player, ItemStack stack) {
		ResourceLocation tableId = LootBagItem.getLootTable(stack);
		if (tableId == null) return false;

		if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
			LootTable table = serverLevel.getServer().getLootData().getLootTable(tableId);

			LootParams.Builder paramBuilder = new LootParams.Builder(serverLevel)
				.withParameter(LootContextParams.THIS_ENTITY, player)
				.withParameter(LootContextParams.ORIGIN, player.position())
				.withParameter(ESLootContextParams.BOSS_CHALLENGE_COUNTS, LootBagItem.getBossChallengeCounts(stack));
			LootParams params = paramBuilder.create(ESLootContextParamSets.BOSS);

			for (ItemStack loot : table.getRandomItems(params)) {
				ItemEntity itemEntity = new ItemEntity(
					level,
					player.getX(),
					player.getY(),
					player.getZ(),
					loot
				);

				ESDataAttachments.IMPORTANT_ITEM.setData(itemEntity, true);
				itemEntity.setNoPickUpDelay();
				itemEntity.setTarget(player.getUUID());
				itemEntity.setExtendedLifetime();
				level.addFreshEntity(itemEntity);
			}

			stack.shrink(1);
		}

		return true;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		dropLoot(level, player, stack);
		return InteractionResultHolder.consume(stack);
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
		if (other.isEmpty() && action == ClickAction.SECONDARY) {
			return dropLoot(player.level(), player, stack);
		}
		return super.overrideOtherStackedOnMe(stack, other, slot, action, player, access);
	}
}
