package cn.leolezury.eternalstarlight.common.mixin;

import cn.leolezury.eternalstarlight.common.util.GalacticQuiverUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {

	@Inject(method = "loadProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;split(I)Lnet/minecraft/world/item/ItemStack;"))
	private static void consumeQuiverProjectile(LivingEntity shooter, ItemStack crossbowStack, ItemStack ammoStack, boolean hasAmmo, boolean isCreative, CallbackInfoReturnable<Boolean> cir) {
		if (!(shooter instanceof Player player)) return;
		if (!GalacticQuiverUtil.isFromQuiver(ammoStack)) return;
		GalacticQuiverUtil.consumeFromQuiver(player, ammoStack);
	}
}