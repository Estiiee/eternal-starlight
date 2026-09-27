package cn.leolezury.eternalstarlight.common.mixin;

import cn.leolezury.eternalstarlight.common.item.misc.GalacticQuiverItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {

	@WrapOperation(
		method = "tryPickup",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"
		)
	)
	public boolean addToInventory(Inventory instance, ItemStack itemStack, Operation<Boolean> original) {
		if (itemStack.is(ItemTags.ARROWS)) {
			boolean arrowSuccess = GalacticQuiverItem.addArrowToInventory(instance, itemStack);
			return arrowSuccess || original.call(instance, itemStack);
		}

		return original.call(instance, itemStack);
	}
}
