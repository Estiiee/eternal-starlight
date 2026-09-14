package cn.leolezury.eternalstarlight.common.platform;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.block.TearBombBlock;
import cn.leolezury.eternalstarlight.common.block.fluid.EtherFluid;
import cn.leolezury.eternalstarlight.common.client.model.armor.StarlitDiamondArmorModel;
import cn.leolezury.eternalstarlight.common.item.armor.AlchemistArmorItem;
import cn.leolezury.eternalstarlight.common.item.armor.ThermalSpringstoneArmorItem;
import cn.leolezury.eternalstarlight.common.item.armor.UnrealiumArmorItem;
import cn.leolezury.eternalstarlight.common.item.combat.CrescentSpearItem;
import cn.leolezury.eternalstarlight.common.item.combat.HammerItem;
import cn.leolezury.eternalstarlight.common.item.combat.PetalScytheItem;
import cn.leolezury.eternalstarlight.common.item.combat.ScytheItem;
import cn.leolezury.eternalstarlight.common.network.ESPacket;
import cn.leolezury.eternalstarlight.common.platform.registry.RegistrationProvider;
import cn.leolezury.eternalstarlight.common.registry.ESItems;
import cn.leolezury.eternalstarlight.common.resource.gatekeeper.TheGatekeeperNameManager;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.*;

public interface ESPlatform {
	ESPlatform INSTANCE = new ESForgePlatform(); //loom architectury remnant

	enum Loader {
		NEOFORGE,
		FABRIC,
		FORGE
	}

	// some loader-related stuff
	Loader getLoader();

	boolean isPhysicalClient();

	Path getConfigDir();

	// registry utils
	<T> RegistrationProvider<T> createRegistrationProvider(ResourceKey<? extends Registry<T>> key, String namespace);

	<T> RegistrationProvider<T> createNewRegistryProvider(ResourceKey<? extends Registry<T>> key, String namespace);

	<T> void registerDatapackRegistry(ResourceKey<Registry<T>> key, Codec<T> codec, Codec<T> networkCodec);

	// for initialization
	// items
	default SpawnEggItem createSpawnEgg(Supplier<EntityType<? extends Mob>> defaultType, int backgroundColor, int highlightColor, Item.Properties properties) {
		return new SpawnEggItem(defaultType.get(), backgroundColor, highlightColor, properties);
	}

	default Item createMobBucket(Supplier<? extends EntityType<? extends Mob>> type, Fluid fluid, SoundEvent sound, Item.Properties props) {
		return new MobBucketItem(type.get(), fluid, sound, props);
	}

	default ScytheItem createScythe(Tier tier, boolean canTill, Item.Properties properties) {
		return new ScytheItem(tier, canTill, 4, -2.7F, 0.0F, 0.0F, properties);
	}

	default ScytheItem createPetalScythe(Tier tier, boolean canTill, Item.Properties props) {
		return new PetalScytheItem(tier, canTill, 4, -2.7F, 0.5F, 0.45F, props);
	}

	default HammerItem createHammer(Tier tier, Supplier<ParticleOptions> smashParticle, Holder<SoundEvent> smashSound, Item.Properties properties) {
		return new HammerItem(tier, smashParticle, smashSound, 7, -3.0F, properties);
	}

	default CrescentSpearItem createCrescentSpear(Tier tier, Item.Properties props) {
		return new CrescentSpearItem(tier, 7.5F, -2.7F, props);
	}

	default ThermalSpringstoneArmorItem createThermalSpringstoneArmor(ArmorMaterial material, ArmorItem.Type type, Item.Properties properties) {
		return new ThermalSpringstoneArmorItem(material, type, properties);
	}

	default AlchemistArmorItem createAlchemistArmor(ArmorMaterial material, ArmorItem.Type type, Item.Properties properties) {
		return new AlchemistArmorItem(material, type, properties);
	}

	default ArmorItem createStarlitDiamondArmor(ArmorMaterial material, ArmorItem.Type type, Item.Properties properties) {
		return new ArmorItem(material, type, properties) {
			@Override
			public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
				super.inventoryTick(stack, level, entity, slot, selected);

				if (level.getGameTime() % 20 == 0 && !level.isClientSide && level.getGameTime() % 20 == 0) {
					stack.getOrCreateTag().putInt("AccessorySlotCount", 3);
				}
			}

			@Override
			public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
				return EternalStarlight.id("textures/armor/starlit_diamond_layer_" + ((slot == EquipmentSlot.LEGS) ? "2.png" : "1.png")).toString();
			}

			@Override
			public void initializeClient(Consumer<IClientItemExtensions> consumer) {
				consumer.accept(new IClientItemExtensions() {
					private StarlitDiamondArmorModel<LivingEntity> innerModel;
					private StarlitDiamondArmorModel<LivingEntity> outerModel;

					@Override
					public @NotNull HumanoidModel<?> getHumanoidArmorModel(
						LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original
					) {
						if (innerModel == null || outerModel == null) {
							var models = Minecraft.getInstance().getEntityModels();
							innerModel = new StarlitDiamondArmorModel<>(models.bakeLayer(StarlitDiamondArmorModel.INNER_LOCATION));
							outerModel = new StarlitDiamondArmorModel<>(models.bakeLayer(StarlitDiamondArmorModel.OUTER_LOCATION));
						}

						if (stack.is(ESItems.STARLIT_DIAMOND_LEGGINGS.get()))
							return innerModel;

						if (stack.is(ESItems.STARLIT_DIAMOND_HELMET.get())
							|| stack.is(ESItems.STARLIT_DIAMOND_CHESTPLATE.get())
							|| stack.is(ESItems.STARLIT_DIAMOND_BOOTS.get()))
							return outerModel;

						return original;
					}
				});
			}
		};
	}

	default UnrealiumArmorItem createUnrealiumArmor(ArmorMaterial material, ArmorItem.Type type, Item.Properties properties) {
		return new UnrealiumArmorItem(material, type, properties);
	}

	CreativeModeTab getESTab();

	// blocks
	default FlowerPotBlock createFlowerPot(Supplier<FlowerPotBlock> pot, Supplier<? extends Block> flower, BlockBehaviour.Properties properties) {
		return new FlowerPotBlock(flower.get(), properties);
	}

	default EtherFluid.Still createEtherFluid() {
		return new EtherFluid.Still();
	}

	default EtherFluid.Flowing createFlowingEtherFluid() {
		return new EtherFluid.Flowing();
	}

	default TearBombBlock createTearBombBlock(BlockBehaviour.Properties properties) {
		return new TearBombBlock(properties);
	}

	// attachment
	default <T> EntityDataAttachment<T> registerDataAttachment(String id, Supplier<T> defaultValue, boolean copyOnDeath, BiPredicate<T, T> shouldSync, BiConsumer<FriendlyByteBuf, T> writer, Function<FriendlyByteBuf, T> reader) {
		return createAttachment(EternalStarlight.id(id), defaultValue, copyOnDeath, shouldSync, writer, reader);
	}

	<T> EntityDataAttachment<T> createAttachment(ResourceLocation id, Supplier<T> defaultValue, boolean copyOnDeath, BiPredicate<T, T> shouldSync, BiConsumer<FriendlyByteBuf, T> writer, Function<FriendlyByteBuf, T> reader);

	// reload listeners
	default TheGatekeeperNameManager createGatekeeperNameManager() {
		return new TheGatekeeperNameManager();
	}

	// event-related
	default boolean canEntityGrief(Level level, Entity entity) {
		return level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
	}

	default boolean postEntityDestroyBlockEvent(Level level, BlockPos pos, Entity entity) {
		if (entity instanceof ServerPlayer player && (!level.mayInteract(player, pos) || player.blockActionRestricted(level, pos, player.gameMode.getGameModeForPlayer()))) {
			return false;
		}
		return level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
	}

	default boolean postTravelToDimensionEvent(Entity entity, ResourceKey<Level> dimension) {
		return true;
	}

	default boolean postTeleportEvent(Entity entity, Vec3 destPos) {
		return true;
	}

	// item stuff
	boolean isShears(ItemStack stack);

	boolean isShield(ItemStack stack);

	default boolean canScrape(ItemStack stack) {
		return stack.is(ItemTags.AXES);
	}

	Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> getToolTillAction(UseOnContext context);

	default int getBurnTime(ItemStack stack, RecipeType<?> type) {
		return AbstractFurnaceBlockEntity.getFuel().get(stack.getItem());
	}

	default boolean hasCraftingRemainingItem(ItemStack stack) {
		return stack.getItem().hasCraftingRemainingItem();
	}

	default Optional<ItemStack> getCraftingRemainingItem(ItemStack stack) {
		return Optional.ofNullable(stack.getItem().getCraftingRemainingItem()).map(ItemStack::new);
	}

	// dispenser
	default boolean canBoatInFluid(Boat boat, FluidState state) {
		return state.is(FluidTags.WATER);
	}

	// part entity
	default Entity getPartEntityParent(Entity entity) {
		if (entity instanceof EnderDragonPart part) {
			return part.parentMob;
		}
		return null;
	}

	// networking
	void sendToClient(ServerPlayer player, ESPacket packet);
	void sendToTrackingClientsImpl(ServerLevel level, Entity entity, ESPacket packet);

	default void sendToAllClients(ServerLevel level, ESPacket packet) {
		for (ServerPlayer player : level.players()) {
			sendToClient(player, packet);
		}
	}
	default void sendToTrackingClients(ServerLevel level, Entity entity, ESPacket packet) {
		sendToTrackingClientsImpl(level, entity, packet);
	}

	// attributes
	Attribute getReach();
	Attribute getBlockReach();

}
