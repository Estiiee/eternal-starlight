package cn.leolezury.eternalstarlight.common.enchantment;

import cn.leolezury.eternalstarlight.common.data.ESEnchantments;
import cn.leolezury.eternalstarlight.common.util.ESTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class FertileEnchantment extends Enchantment {

	public FertileEnchantment() {
		super(Rarity.COMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{ EquipmentSlot.MAINHAND });
	}

	@Override
	public int getMinCost(int level) {
		return 20 + (level - 1) * 11;
	}

	@Override
	public int getMaxCost(int level) {
		return 30 + (level - 1) * 11;
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public boolean canEnchant(ItemStack stack) {
		return stack.is(ESTags.Items.SEEDS_LAUNCHER_ENCHANTABLE) || super.canEnchant(stack);
	}

	public static int getExtraProjectiles(ItemStack stack) {
		Enchantment ench = BuiltInRegistries.ENCHANTMENT.get(ESEnchantments.FERTILE.getResourceKey());
		int level = EnchantmentHelper.getItemEnchantmentLevel(ench, stack);
		return level * 2;
	}
}
