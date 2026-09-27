package cn.leolezury.eternalstarlight.common.util;

import cn.leolezury.eternalstarlight.common.item.component.LargeItemStackList;
import cn.leolezury.eternalstarlight.common.item.misc.GalacticQuiverItem;
import cn.leolezury.eternalstarlight.common.registry.ESItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GalacticQuiverUtil {

	private static final String FROM_QUIVER = "FromQuiver";

	private GalacticQuiverUtil() {}

	public static boolean isFromQuiver(ItemStack stack) {
		return stack.hasTag() && stack.getTag().getBoolean(FROM_QUIVER);
	}

	public static void markFromQuiver(ItemStack stack) {
		stack.getOrCreateTag().putBoolean(FROM_QUIVER, true);
	}

	public static void consumeFromQuiver(Player player, ItemStack ammo) {
		if (!isFromQuiver(ammo)) return;

		ItemStack compare = ammo.copy();

		CompoundTag tag = compare.getTag();
		if (tag != null) {
			tag.remove(FROM_QUIVER);

			if (tag.isEmpty()) {
				compare.setTag(null);
			}
		}

		Inventory inv = player.getInventory();

		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack quiver = inv.getItem(i);

			if (!quiver.is(ESItems.GALACTIC_QUIVER.get())) {
				continue;
			}

			LargeItemStackList list = GalacticQuiverItem.getArrows(quiver);
			List<LargeItemStackList.LargeItemStack> arrows = new ArrayList<>(list);

			for (LargeItemStackList.LargeItemStack arrow : arrows) {
				if (ItemStack.isSameItemSameTags(arrow.getItem(), compare)) {
					arrow.shrink(1);

					arrows.removeIf(LargeItemStackList.LargeItemStack::isEmpty);

					GalacticQuiverItem.setArrows(
						quiver,
						new LargeItemStackList(Collections.unmodifiableList(arrows))
					);

					return;
				}
			}
		}
	}
}