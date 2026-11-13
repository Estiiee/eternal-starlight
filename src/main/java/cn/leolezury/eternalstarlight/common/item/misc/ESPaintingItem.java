package cn.leolezury.eternalstarlight.common.item.misc;

import cn.leolezury.eternalstarlight.common.entity.misc.ESPainting;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class ESPaintingItem extends HangingEntityItem {
	private static final Component TOOLTIP_RANDOM_VARIANT =
		Component.translatable("painting.random").withStyle(ChatFormatting.GRAY);

	public ESPaintingItem(Properties properties) {
		// .get() crashes, but vanilla paintings here are hardcoded for vanilla types anyway
		//super(ESEntities.PAINTING.get(), properties);
		super(EntityType.PAINTING, properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		BlockPos pos = context.getClickedPos();
		Direction dir = context.getClickedFace();
		BlockPos placePos = pos.relative(dir);
		Player player = context.getPlayer();
		ItemStack stack = context.getItemInHand();

		if (player != null && !this.mayPlace(player, dir, stack, placePos)) {
			return InteractionResult.FAIL;
		}

		Level level = context.getLevel();
		Optional<ESPainting> opt = ESPainting.createPainting(level, stack.copy(), placePos, dir);
		if (opt.isEmpty()) {
			return InteractionResult.CONSUME;
		}

		HangingEntity entity = opt.get();

		CompoundTag tag = stack.getTagElement("EntityTag");
		if (tag != null) {
			EntityType.updateCustomEntityTag(level, player, entity, tag);
		}

		if (entity.survives()) {
			if (!level.isClientSide) {
				entity.playPlacementSound();
				level.gameEvent(player, GameEvent.ENTITY_PLACE, entity.position());
				level.addFreshEntity(entity);
				stack.shrink(1);
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		}

		return InteractionResult.CONSUME;
	}
}