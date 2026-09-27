package cn.leolezury.eternalstarlight.common.mixin;

import cn.leolezury.eternalstarlight.common.item.component.LargeItemStackList;
import cn.leolezury.eternalstarlight.common.item.misc.GalacticQuiverItem;
import cn.leolezury.eternalstarlight.common.registry.ESItems;
import cn.leolezury.eternalstarlight.common.util.GalacticQuiverUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(ProjectileWeaponItem.class)
public abstract class ProjectileWeaponItemMixin {

	@Inject(method = "getHeldProjectile", at = @At("RETURN"), cancellable = true)
	private static void getHeldProjectile(LivingEntity livingEntity, Predicate<ItemStack> predicate, CallbackInfoReturnable<ItemStack> cir) {
		if (!cir.getReturnValue().isEmpty()) return;
		if (!(livingEntity instanceof Player player)) return;

		Inventory inv = player.getInventory();

		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack quiver = inv.getItem(i);

			if (!quiver.is(ESItems.GALACTIC_QUIVER.get())) {
				continue;
			}

			LargeItemStackList arrows = GalacticQuiverItem.getArrows(quiver);

			for (LargeItemStackList.LargeItemStack arrow : arrows) {
				if (predicate.test(arrow.getItem())) {
					ItemStack ammo = arrow.getItem().copy();
					GalacticQuiverUtil.markFromQuiver(ammo);

					cir.setReturnValue(ammo);
					return;
				}
			}
		}
	}
}