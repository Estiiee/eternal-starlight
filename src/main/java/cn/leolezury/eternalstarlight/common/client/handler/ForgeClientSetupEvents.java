package cn.leolezury.eternalstarlight.common.client.handler;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.gui.tooltip.ClientGalacticQuiverTooltip;
import cn.leolezury.eternalstarlight.common.item.tooltip.GalacticQuiverTooltipComponent;
import cn.leolezury.eternalstarlight.common.platform.ESClientPlatform;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.io.IOException;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = EternalStarlight.ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeClientSetupEvents {

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(ESClientSetupHandler::clientSetup);
	}

	@SubscribeEvent
	public static void onRegisterDimEffects(RegisterDimensionSpecialEffectsEvent event) {
		event.register(EternalStarlight.id("special_effect"), ESClientPlatform.INSTANCE.getDimEffect());
	}

	@SubscribeEvent
	public static void onRegisterBlockColor(RegisterColorHandlersEvent.Block event) {
		ESClientSetupHandler.registerBlockColors(event::register);
	}

	@SubscribeEvent
	public static void onRegisterItemColor(RegisterColorHandlersEvent.Item event) {
		ESClientSetupHandler.registerItemColors(event::register);
	}

	/* should be done but in case still here for reference
	@SubscribeEvent
	private static void onRegisterClientExtensions(IClientItemExtensions event) {
		event.registerItem(ESForgeItemStackRenderer.CLIENT_ITEM_EXTENSION, ESItems.GLACITE_SHIELD.get());
		event.registerItem(ESForgeItemStackRenderer.CLIENT_ITEM_EXTENSION, ESItems.FLOWGLAZE_SHIELD.get());
		IClientItemExtensions alchemistArmor = new IClientItemExtensions() {
			private AlchemistArmorModel<LivingEntity> model;

			@Override
			public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
				if (model == null) {
					model = new AlchemistArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(AlchemistArmorModel.LAYER_LOCATION));
				}

				return model;
			}
		};
		event.registerItem(alchemistArmor, ESItems.ALCHEMIST_MASK.get());
		event.registerItem(alchemistArmor, ESItems.ALCHEMIST_ROBE.get());
		IClientItemExtensions thermalSpringstoneArmor = new IClientItemExtensions() {
			private ThermalSpringStoneArmorModel<LivingEntity> innerModel;
			private ThermalSpringStoneArmorModel<LivingEntity> outerModel;

			@Override
			public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
				if (innerModel == null || outerModel == null) {
					innerModel = new ThermalSpringStoneArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ThermalSpringStoneArmorModel.INNER_LOCATION));
					outerModel = new ThermalSpringStoneArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ThermalSpringStoneArmorModel.OUTER_LOCATION));
				}

				if (itemStack.is(ESItems.THERMAL_SPRINGSTONE_HELMET.get()) || itemStack.is(ESItems.THERMAL_SPRINGSTONE_CHESTPLATE.get()) || itemStack.is(ESItems.THERMAL_SPRINGSTONE_BOOTS.get())) {
					return outerModel;
				} else if (itemStack.is(ESItems.THERMAL_SPRINGSTONE_LEGGINGS.get())) {
					return innerModel;
				}

				return IClientItemExtensions.super.getHumanoidArmorModel(livingEntity, itemStack, equipmentSlot, original);
			}
		};
		event.registerItem(thermalSpringstoneArmor, ESItems.THERMAL_SPRINGSTONE_HELMET.get());
		event.registerItem(thermalSpringstoneArmor, ESItems.THERMAL_SPRINGSTONE_CHESTPLATE.get());
		event.registerItem(thermalSpringstoneArmor, ESItems.THERMAL_SPRINGSTONE_LEGGINGS.get());
		event.registerItem(thermalSpringstoneArmor, ESItems.THERMAL_SPRINGSTONE_BOOTS.get());
		IClientItemExtensions starlitDiamondArmor = new IClientItemExtensions() {
			private StarlitDiamondArmorModel<LivingEntity> innerModel;
			private StarlitDiamondArmorModel<LivingEntity> outerModel;

			@Override
			public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
				if (innerModel == null || outerModel == null) {
					innerModel = new StarlitDiamondArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(StarlitDiamondArmorModel.INNER_LOCATION));
					outerModel = new StarlitDiamondArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(StarlitDiamondArmorModel.OUTER_LOCATION));
				}

				if (itemStack.is(ESItems.STARLIT_DIAMOND_HELMET.get()) || itemStack.is(ESItems.STARLIT_DIAMOND_CHESTPLATE.get()) || itemStack.is(ESItems.STARLIT_DIAMOND_BOOTS.get())) {
					return outerModel;
				} else if (itemStack.is(ESItems.STARLIT_DIAMOND_LEGGINGS.get())) {
					return innerModel;
				}

				return IClientItemExtensions.super.getHumanoidArmorModel(livingEntity, itemStack, equipmentSlot, original);
			}
		};
		event.registerItem(starlitDiamondArmor, ESItems.STARLIT_DIAMOND_HELMET.get());
		event.registerItem(starlitDiamondArmor, ESItems.STARLIT_DIAMOND_CHESTPLATE.get());
		event.registerItem(starlitDiamondArmor, ESItems.STARLIT_DIAMOND_LEGGINGS.get());
		event.registerItem(starlitDiamondArmor, ESItems.STARLIT_DIAMOND_BOOTS.get());
		IClientItemExtensions unrealiumArmor = new IClientItemExtensions() {
			private UnrealiumArmorModel<LivingEntity> innerModel;
			private UnrealiumArmorModel<LivingEntity> outerModel;

			@Override
			public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
				if (innerModel == null || outerModel == null) {
					innerModel = new UnrealiumArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(UnrealiumArmorModel.INNER_LOCATION));
					outerModel = new UnrealiumArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(UnrealiumArmorModel.OUTER_LOCATION));
				}

				if (itemStack.is(ESItems.UNREALIUM_HELMET.get()) || itemStack.is(ESItems.UNREALIUM_CHESTPLATE.get()) || itemStack.is(ESItems.UNREALIUM_BOOTS.get())) {
					return outerModel;
				} else if (itemStack.is(ESItems.UNREALIUM_LEGGINGS.get())) {
					return innerModel;
				}

				return IClientItemExtensions.super.getHumanoidArmorModel(livingEntity, itemStack, equipmentSlot, original);
			}
		};
		event.registerItem(unrealiumArmor, ESItems.UNREALIUM_HELMET.get());
		event.registerItem(unrealiumArmor, ESItems.UNREALIUM_CHESTPLATE.get());
		event.registerItem(unrealiumArmor, ESItems.UNREALIUM_LEGGINGS.get());
		event.registerItem(unrealiumArmor, ESItems.UNREALIUM_BOOTS.get());
		event.registerItem(ESForgeItemStackRenderer.CLIENT_ITEM_EXTENSION, ESItems.MALARITE_SPEAR.get());
		event.registerItem(ESForgeItemStackRenderer.CLIENT_ITEM_EXTENSION, ESItems.PUNGENCY_FRUIT_SPEAR.get());
		event.registerItem(ESForgeItemStackRenderer.CLIENT_ITEM_EXTENSION, ESItems.CRESCENT_SPEAR.get());
		event.registerItem(ESForgeItemStackRenderer.CLIENT_ITEM_EXTENSION, ESItems.LOOT_CHEST.get());

		event.registerFluidType(new IClientFluidTypeExtensions() {
			@Override
			public ResourceLocation getStillTexture() {
				return EternalStarlight.id("block/ether");
			}

			@Override
			public ResourceLocation getFlowingTexture() {
				return EternalStarlight.id("block/ether_flow");
			}

			@Override
			public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
				return ESClientHandler.getEtherTint(getter, pos);
			}

			@Override
			public @NotNull Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
				return new Vector3f(232 / 255F, 255 / 255F, 222 / 255F);
			}

			@Override
			public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
				RenderSystem.setShaderFogStart(0.0F);
				RenderSystem.setShaderFogEnd(3.0F);
			}
		}, ESFluidTypes.ETHER.get());
	}
	 */

	@SubscribeEvent
	public static void onBakingCompleted(ModelEvent.ModifyBakingResult event) {
		Map<ResourceLocation, BakedModel> models = event.getModels();
		ESClientSetupHandler.modifiedBakedModels = false;
		ESClientSetupHandler.modifyBakingResult(models);
	}

	@SubscribeEvent
	public static void onRegisterExtraModels(ModelEvent.RegisterAdditional event) {
		ESClientSetupHandler.registerExtraBakedModels(l -> {
			ModelResourceLocation forged = new ModelResourceLocation(
				new ResourceLocation(l.getNamespace(), "item/" + l.getPath()),
				"inventory"
			);

			event.register(forged);
		});
	}


	@SubscribeEvent
	public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
		ESClientSetupHandler.registerParticleProviders(event::registerSpriteSet);
	}

	@SubscribeEvent
	public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		ESClientSetupHandler.registerEntityRenderers(event::registerEntityRenderer);
		ESClientSetupHandler.registerBlockEntityRenderers(event::registerBlockEntityRenderer);
	}

	@SubscribeEvent
	public static void onRegisterSkullModels(EntityRenderersEvent.CreateSkullModels event) {
		ESClientSetupHandler.registerSkullModels(event::registerSkullModel, event.getEntityModelSet());
	}

	@SubscribeEvent
	public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		ESClientSetupHandler.registerLayers(event::registerLayerDefinition);
	}

	/*
	@SubscribeEvent
	private static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
		ESClientSetupHandler.registerMenuScreens(event::register);
	}
	 */

	@SubscribeEvent
	public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
		/* Not doable as easily as far as I know, and not important enough for me to care
		for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
			if (type.getBaseClass() != null && LivingEntity.class.isAssignableFrom(type.getBaseClass())) {
				LivingEntityRenderer<?, ?> renderer = event.getRenderer((EntityType<? extends LivingEntity>) type);
				if (renderer != null) {
					ESClientSetupHandler.onRenderLayerAttachment(type, renderer, event.getContext());
				}
			}
		}
		*/
		for (String skin : event.getSkins()) {
			LivingEntityRenderer<?, ?> renderer = event.getSkin(skin);
			if (renderer != null) {
				ESClientSetupHandler.onRenderLayerAttachment(EntityType.PLAYER, renderer, event.getContext());
			}
		}
	}

	@SubscribeEvent
	public static void onRegisterShader(RegisterShadersEvent event) {
		ESClientSetupHandler.registerShaders((location, format, loaded) -> {
			try {
				event.registerShader(new ShaderInstance(event.getResourceProvider(), location, format), loaded);
			} catch (IOException e) {
				EternalStarlight.LOGGER.error("Cannot register shader: {}", location);
			}
		});
	}

	@SubscribeEvent
	public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {

		event.registerAbove(
			VanillaGuiOverlay.VIGNETTE.id(),
			"offhand_attack_indicator",
			(gui, guiGraphics, partialTicks, width, height) ->
				ESClientHandler.renderOffhandAttackIndicator(guiGraphics)
		);

		event.registerAbove(
			VanillaGuiOverlay.CROSSHAIR.id(),
			"spell_crosshair",
			(gui, guiGraphics, partialTicks, width, height) ->
				ESClientHandler.renderSpellCrosshair(guiGraphics, width, height)
		);

		event.registerAbove(
			VanillaGuiOverlay.VIGNETTE.id(),
			("ether_erosion"),
			(gui, guiGraphics, partialTicks, width, height) ->
				ESClientHandler.renderEtherErosion(guiGraphics)
		);

		event.registerAbove(
			VanillaGuiOverlay.ARMOR_LEVEL.id(),
			("ether_armor"),
			(gui, guiGraphics, partialTicks, width, height) -> {
				if (Minecraft.getInstance().gameMode != null &&
					Minecraft.getInstance().gameMode.canHurtPlayer()) {
					ESClientHandler.renderEtherArmor(guiGraphics, width, height);
				}
			}
		);

		event.registerAbove(
			VanillaGuiOverlay.VIGNETTE.id(),
			("orb_of_prophecy_use"),
			(gui, guiGraphics, partialTicks, width, height) ->
				ESClientHandler.renderOrbOfProphecyUse(guiGraphics)
		);

		event.registerAbove(
			VanillaGuiOverlay.VIGNETTE.id(),
			("dream_catcher"),
			(gui, guiGraphics, partialTicks, width, height) ->
				ESClientHandler.renderDreamCatcher(guiGraphics)
		);

		event.registerAbove(
			VanillaGuiOverlay.VIGNETTE.id(),
			("current_crest"),
			(gui, guiGraphics, partialTicks, width, height) ->
				ESClientHandler.renderCurrentCrest(guiGraphics)
		);

		event.registerAbove(
			VanillaGuiOverlay.VIGNETTE.id(),
			("carved_lunaris_cactus_fruit_blur"),
			(gui, guiGraphics, partialTicks, width, height) ->
				ESClientHandler.renderCarvedLunarisCactusFruitBlur(guiGraphics)
		);

		event.registerAbove(
			VanillaGuiOverlay.PORTAL.id(),
			("portal"),
			(gui, guiGraphics, partialTicks, width, height) ->
				ESClientHandler.renderPortalOverlay(guiGraphics)
		);
	}

	@SubscribeEvent
	public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
		for (Map.Entry<ResourceLocation, KeyMapping> mapping : ESClientSetupHandler.KEY_MAPPINGS.entrySet()) {
			event.register(mapping.getValue());
		}
	}

	@SubscribeEvent
	public static void onAddReloadListener(RegisterClientReloadListenersEvent event) {
		ESClientSetupHandler.addClientReloadListeners(event::registerReloadListener);
	}

	@SubscribeEvent
	public static void onRegisterClientTooltipComponentFactories(RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(GalacticQuiverTooltipComponent.class, component -> new ClientGalacticQuiverTooltip(component.contents()));
	}
}
