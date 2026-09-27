package cn.leolezury.eternalstarlight.common.mixin;

import cn.leolezury.eternalstarlight.common.util.GalacticQuiverUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BowItem.class)
public abstract class BowItemMixin {

	@Inject(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
	private void consumeQuiverProjectile(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft, CallbackInfo ci) {
		if (!(entityLiving instanceof Player player)) return;
		ItemStack itemstack = player.getProjectile(stack);
		if (!GalacticQuiverUtil.isFromQuiver(itemstack)) return;
		GalacticQuiverUtil.consumeFromQuiver(player, itemstack);
	}
}