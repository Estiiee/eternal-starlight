package cn.leolezury.eternalstarlight.common.mixin;

import cn.leolezury.eternalstarlight.common.handler.ESCommonHandler;
import cn.leolezury.eternalstarlight.common.item.component.Accessory;
import cn.leolezury.eternalstarlight.common.registry.ESAccessories;
import cn.leolezury.eternalstarlight.common.registry.ESItems;
import cn.leolezury.eternalstarlight.common.util.ESAccessoryUtil;
import cn.leolezury.eternalstarlight.common.util.ESTags;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@Shadow
	public abstract boolean is(TagKey<Item> arg);

	@Shadow
	public abstract void setDamageValue(int i);

	@Shadow
	public abstract int getDamageValue();

	@Shadow
	public abstract boolean isDamaged();

	@Shadow
	public abstract boolean is(Item item);

	@Inject(method = "inventoryTick", at = @At("RETURN"))
	private void inventoryTick(Level level, Entity entity, int inventorySlot, boolean isCurrentItem, CallbackInfo ci) {
		if (!level.isClientSide && entity.tickCount % 600 == 0 && isDamaged() && (is(ESTags.Items.MENDS_NATURALLY) || (is(ESTags.Items.REPAIRED_BY_CRESCENT_PENDANT) && entity instanceof LivingEntity living && ESAccessoryUtil.getActiveAccessoriesOnArmors(living).contains(ESItems.CRESCENT_PENDANT.get())))) {
			setDamageValue(Math.max(getDamageValue() - 1, 0));
		}
	}

	@Inject(
		method = "getAttributeModifiers(Lnet/minecraft/world/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;",
		at = @At("RETURN"),
		cancellable = true
	)
	private void injectAccessoryModifiers(EquipmentSlot slot, CallbackInfoReturnable<Multimap<Attribute, AttributeModifier>> cir) {
		ItemStack stack = (ItemStack) (Object) this;

		if (!(stack.getItem() instanceof ArmorItem armor)) return;

		if (armor.getEquipmentSlot() != slot) return;

		List<ItemStack> accessories = ESAccessoryUtil.getAccessoryStacks(stack);
		if (accessories.isEmpty()) return;

		Multimap<Attribute, AttributeModifier> modifiers = cir.getReturnValue();
		if (modifiers == null) return;

		Multimap<Attribute, AttributeModifier> newMap = HashMultimap.create(modifiers);

		for (ItemStack accStack : accessories) {
			Accessory accessory = ESAccessories.get(accStack);
			if (accessory == null) continue;

			newMap.putAll(accessory.attributeModifiers());
		}

		cir.setReturnValue(newMap);
	}

	@Inject(
		method = "getTooltipLines(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/Item;appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;)V"
		)
	)
	private void es$injectTooltip(Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir, @Local(ordinal = 0) List<Component> list) {
		ItemStack stack = (ItemStack)(Object)this;
		ESCommonHandler.onItemTooltip(player, flag, stack, list);
	}

	@ModifyReturnValue(method = "overrideStackedOnOther", at = @At("RETURN"))
	private boolean overrideStackedOnOther(boolean original, @Local(argsOnly = true) Slot slot, @Local(argsOnly = true) ClickAction action, @Local(argsOnly = true) Player player) {
		return original || ESAccessoryUtil.overrideEquipmentOnAccessory((ItemStack) (Object) this, slot, action, player);
	}

	@ModifyReturnValue(method = "overrideOtherStackedOnMe", at = @At("RETURN"))
	private boolean overrideOtherStackedOnMe(boolean original, @Local(argsOnly = true) ItemStack other, @Local(argsOnly = true) Slot slot, @Local(argsOnly = true) ClickAction action, @Local(argsOnly = true) Player player, @Local(argsOnly = true) SlotAccess access) {
		return original || ESAccessoryUtil.overrideAccessoryOnEquipment((ItemStack) (Object) this, other, slot, action, player, access);
	}
}
