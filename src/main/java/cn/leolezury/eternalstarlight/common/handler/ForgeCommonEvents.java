package cn.leolezury.eternalstarlight.common.handler;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.data.ESEnchantments;
import cn.leolezury.eternalstarlight.common.enchantment.SwiftLashEnchantment;
import cn.leolezury.eternalstarlight.common.registry.ESAttributes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.*;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

public class ForgeCommonEvents {

	@Mod.EventBusSubscriber(modid = EternalStarlight.ID)
	public static class ForgeEvents {

		@SubscribeEvent
		public static void onServerTick(TickEvent.ServerTickEvent event) {
			if (event.phase == TickEvent.Phase.END) {
				ESCommonHandler.onServerTick(event.getServer());
			}
		}

		@SubscribeEvent
		public static void onLevelTick(TickEvent.LevelTickEvent event) {
			if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel serverLevel) {
				ESCommonHandler.onLevelTick(serverLevel);
			}
		}

		@SubscribeEvent
		public static void onLevelLoad(LevelEvent.Load event) {
			if (event.getLevel() instanceof ServerLevel serverLevel) {
				ESCommonHandler.onLevelLoad(serverLevel);
			}
		}

		@SubscribeEvent
		public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
			ESCommonHandler.onPlayerJoin(event.getEntity());
		}

		@SubscribeEvent
		public static void onIncomingDamage(LivingAttackEvent event) {
			if (event.isCanceled()) return;

			boolean allow = ESCommonHandler.onAllowLivingHurt(
				event.getEntity(),
				event.getSource(),
				event.getAmount()
			);

			if (!allow) {
				event.setCanceled(true);
				return;
			}

			// so neoforge allowed modifying damage here - forge does not.
		}

		@SubscribeEvent
		public static void onLivingHurt(LivingHurtEvent event) {

			LivingEntity entity = event.getEntity();
			DamageSource source = event.getSource();
			float amount = event.getAmount();

			float modified = ESCommonHandler.onModifyLivingHurtDamage(entity, source, amount);
			modified = ESCommonHandler.onModifyLivingActualHurtDamage(entity, source, modified);

			event.setAmount(modified);

			entity.invulnerableTime = ESCommonHandler.onModifyPostAttackInvulnerabilityTicks(entity, source, modified, entity.invulnerableTime);
		}

		@SubscribeEvent
		public static void onLivingDamage(LivingDamageEvent event) {
			ESCommonHandler.onPostLivingHurt(event.getEntity(), event.getSource(), event.getAmount());
		}

		@SubscribeEvent
		public static void onLivingHeal(LivingHealEvent event) {
			event.setAmount(ESCommonHandler.onLivingHeal(event.getEntity(), event.getAmount()));
		}

		@SubscribeEvent
		public static void onLivingDeath(LivingDeathEvent event) {
			if (!event.isCanceled()) {
				boolean allow = ESCommonHandler.onAllowLivingDeath(event.getEntity(), event.getSource());
				if (!allow) {
					event.setCanceled(true);
				}
			}
			if (!event.isCanceled()) {
				ESCommonHandler.onLivingDeath(event.getEntity(), event.getSource());
			}
		}

		@SubscribeEvent
		public static void onLivingVisibility(LivingEvent.LivingVisibilityEvent event) {
			event.modifyVisibility(ESCommonHandler.onLivingVisibility(event.getEntity(), event.getLookingEntity(), event.getVisibilityModifier()));
		}

		@SubscribeEvent
		public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
			LivingEntity target = ESCommonHandler.onLivingChangeTarget(event.getEntity(), event.getNewTarget());
			if (target != event.getNewTarget()) {
				event.setNewTarget(target);
			}
		}

		@SubscribeEvent
		public static void onLivingBreathe(LivingBreatheEvent event) {
			if (!event.canBreathe() && event.getConsumeAirAmount() > 0) {
				int result = ESCommonHandler.onLivingDecreaseAirSupply(event.getEntity());
				if (result > 0) {
					event.setCanBreathe(true);
					event.setRefillAirAmount(result);
				}
				if (result < 0) {
					event.setConsumeAirAmount(Math.max(event.getConsumeAirAmount() + result, 0));
				}
			}
		}

		@SubscribeEvent
		public static void onLivingTick(LivingEvent.LivingTickEvent event) {
			ESCommonHandler.onEntityTick(event.getEntity());
		}

		@SubscribeEvent
		public static void onCriticalHit(CriticalHitEvent event) {
			boolean isCrit = event.getOldDamageModifier() > 1.0F;

			if (isCrit) {
				ESCommonHandler.onCriticalHit(
					event.getEntity(),
					event.getTarget(),
					event.getEntity().getAttackStrengthScale(0.5f)
				);
			}
		}

		@SubscribeEvent
		public static void onBlockBroken(BlockEvent.BreakEvent event) {
			ESCommonHandler.onBlockBroken(event.getPlayer(), event.getPos(), event.getState());
		}

		@SubscribeEvent
		public static void onBlockBreakSpeed(PlayerEvent.BreakSpeed event) {
			event.setNewSpeed(ESCommonHandler.onBlockBreakSpeed(event.getEntity(), event.getState(), event.getNewSpeed()));
		}

		@SubscribeEvent
		public static void onShieldBlock(ShieldBlockEvent event) {
			ESCommonHandler.onShieldBlock(event.getEntity(), event.getDamageSource());
		}

		@SubscribeEvent
		public static void onProjectileImpact(ProjectileImpactEvent event) {
			ESCommonHandler.onProjectileImpact(event.getProjectile(), event.getRayTraceResult());
		}

		@SubscribeEvent
		public static void onCompleteAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
			ESCommonHandler.onCompleteAdvancement(event.getEntity(), event.getAdvancement());
		}

		@SubscribeEvent
		public static void onVanillaGameEvent(VanillaGameEvent event) {
			if (!event.isCanceled()) {
				boolean allow = ESCommonHandler.onVanillaGameEvent(event.getLevel(), event.getVanillaEvent(), event.getEventPosition(), event.getContext());
				if (!allow) {
					event.setCanceled(true);
				}
			}
		}

		@SubscribeEvent
		public static void onAddReloadListener(AddReloadListenerEvent event) {
			ESCommonSetupHandler.addReloadListeners(event::addListener);
		}

		@SubscribeEvent
		public static void onFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
			ESCommonSetupHandler.registerFuels(new ESCommonSetupHandler.FuelRegisterStrategy() {
				@Override
				public void register(ItemLike item, int time) {
					if (event.getItemStack().is(item.asItem())) {
						event.setBurnTime(time);
					}
				}

				@Override
				public void register(TagKey<Item> itemTag, int time) {
					if (event.getItemStack().is(itemTag)) {
						event.setBurnTime(time);
					}
				}
			});
		}

		/*
		@SubscribeEvent
		private static void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
			ESCommonSetupHandler.registerPotions(new ESCommonSetupHandler.BrewingRegisterStrategy() {
				@Override
				public void registerConversion(Holder<Potion> input, Item ingredient, Holder<Potion> output) {
					event.getBuilder().addMix(input, ingredient, output);
				}

				@Override
				public void registerStart(Item ingredient, Holder<Potion> potion) {
					event.getBuilder().addStartMix(ingredient, potion);
				}
			});
		}
		 */

		@SubscribeEvent
		public static void onRegisterCommands(RegisterCommandsEvent event) {
			ESCommonSetupHandler.registerCommands(event.getDispatcher(), event.getBuildContext());
		}

		@SubscribeEvent
		public static void onBlockToolModification(BlockEvent.BlockToolModificationEvent event) {
			ToolAction action = event.getToolAction();

			if (action == ToolActions.AXE_STRIP) {
				for (Map.Entry<Block, Block> entry : ESCommonSetupHandler.STRIPPABLES.get().entrySet()) {
					if (event.getState().is(entry.getKey())) {
						event.setFinalState(entry.getValue().withPropertiesOf(event.getState()));
					}
				}
			}
			else if (action == ToolActions.HOE_TILL) {
				for (Map.Entry<Block, Block> entry : ESCommonSetupHandler.TILLABLES.get().entrySet()) {
					if (event.getState().is(entry.getKey())) {
						event.setFinalState(entry.getValue().withPropertiesOf(event.getState()));
					}
				}
			}
			else if (action == ToolActions.SHOVEL_FLATTEN) {
				for (Map.Entry<Block, Block> entry : ESCommonSetupHandler.FLATTENABLES.get().entrySet()) {
					if (event.getState().is(entry.getKey())) {
						event.setFinalState(entry.getValue().withPropertiesOf(event.getState()));
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onItemAttributes(ItemAttributeModifierEvent event) {
		if (event.getSlotType() != EquipmentSlot.MAINHAND) return;
		int level = EnchantmentHelper.getItemEnchantmentLevel(ESEnchantments.SWIFT_LASH.get(), event.getItemStack());
		if (level > 0) {
			event.addModifier(Attributes.ATTACK_SPEED, new AttributeModifier(
				SwiftLashEnchantment.SWIFT_LASH_UUID,
				"Swift Lash attack speed",
				0.2F * level,
				AttributeModifier.Operation.ADDITION
			));
		}
	}

	@Mod.EventBusSubscriber(modid = EternalStarlight.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class ModEvents {
		@SubscribeEvent
		public void onAttributeModification(EntityAttributeModificationEvent event) {
			event.getTypes().forEach(entityType -> {
				event.add(entityType, ESAttributes.THROWN_POTION_DISTANCE.get());
				event.add(entityType, ESAttributes.ETHER_RESISTANCE.get());
				event.add(entityType, ESAttributes.FIRE_RESISTANCE.get());
				event.add(entityType, ESAttributes.METEOR_COUNTERATTACK_CHANCE.get());
				event.add(entityType, ESAttributes.HEAL_MULTIPLIER.get());
				event.add(entityType, ESAttributes.ENEMY_FOLLOW_RANGE_MULTIPLIER.get());
			});
			event.add(EntityType.PLAYER, ESAttributes.FOG_VISION.get());
		}
	}
}
