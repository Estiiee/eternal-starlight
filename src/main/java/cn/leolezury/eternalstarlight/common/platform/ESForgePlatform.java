package cn.leolezury.eternalstarlight.common.platform;

import cn.leolezury.eternalstarlight.common.block.fluid.EtherFluid;
import cn.leolezury.eternalstarlight.common.block.fluid.ForgeEtherFluid;
import cn.leolezury.eternalstarlight.common.item.misc.DeferredMobBucketItem;
import cn.leolezury.eternalstarlight.common.network.ESPacket;
import cn.leolezury.eternalstarlight.common.network.handler.ESForgeNetworkHandler;
import cn.leolezury.eternalstarlight.common.platform.registry.*;
import cn.leolezury.eternalstarlight.common.registry.ESCreativeModeTabs;
import cn.leolezury.eternalstarlight.common.registry.ESItems;
import cn.leolezury.eternalstarlight.common.util.ESTags;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.*;

public class ESForgePlatform implements ESPlatform {

	public static final List<DeferredRegister<?>> REGISTERS = new ArrayList<>();
	public static final List<RegistryBuilder<?>> CUSTOM_BUILDERS = new ArrayList<>();
	public static final List<CustomRegistryEntry<?>> CUSTOM_REGISTRIES = new ArrayList<>();

	public static IEventBus EVENT_BUS;

	public static void init(IEventBus event) {
		EVENT_BUS = event;
	}

	@Override
	public Loader getLoader() {
		return Loader.FORGE;
	}

	@Override
	public boolean isPhysicalClient() {
		return FMLEnvironment.dist == Dist.CLIENT;
	}

	@Override
	public Path getConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}

	@Override
	public <T> RegistrationProvider<T> createRegistrationProvider(ResourceKey<? extends Registry<T>> key, String namespace) {
		ForgeRegistrationProvider<T> provider = new ForgeRegistrationProvider<>(key, null, namespace);
		if (!REGISTERS.contains(provider.deferredRegister)) {
			REGISTERS.add(provider.deferredRegister);
		}
		return provider;
	}

	@Override
	public <T> RegistrationProvider<T> createNewRegistryProvider(ResourceKey<? extends Registry<T>> key, String namespace) {
		RegistryBuilder<T> builder = new RegistryBuilder<>();
		builder.setName(key.location());
		ForgeCustomRegistrationProvider<T> provider = new ForgeCustomRegistrationProvider<>(key, namespace, builder);
		ESForgePlatform.CUSTOM_REGISTRIES.add(new CustomRegistryEntry<>(builder, provider));

		return provider;
	}


	public static class ForgeRegistrationProvider<T> implements RegistrationProvider<T> {
		private final ResourceKey<? extends Registry<T>> key;
		private final Registry<T> registry;
		private final DeferredRegister<T> deferredRegister;
		private final String namespace;

		ForgeRegistrationProvider(ResourceKey<? extends Registry<T>> key, Registry<T> registry, String namespace) {
			this.key = key;
			this.registry = registry;
			this.deferredRegister = DeferredRegister.create(key, namespace);
			this.namespace = namespace;
		}

		@Override
		public Registry<T> registry() {
			return registry == null ? (Registry<T>) BuiltInRegistries.REGISTRY.get(key.location()) : registry;
		}

		@Override
		public T get(ResourceLocation id) {
			return registry().get(id);
		}

		@Override
		public ResourceLocation getId(T value) {
			return registry().getKey(value);
		}

		@Override
		public Collection<T> values() {
			return registry().stream().toList();
		}

		@Override
		public <I extends T> RegistryObject<T, I> register(String id, Supplier<? extends I> supplier) {
			ResourceLocation location = new ResourceLocation(namespace, id);
			ResourceKey<I> resourceKey = (ResourceKey<I>) ResourceKey.create(key, location);
			net.minecraftforge.registries.RegistryObject<I> forgeObj = deferredRegister.register(id, supplier);
			FakeDeferredHolder<T> fakeHolder = new FakeDeferredHolder<>(resourceKey, key);

			return new RegistryObject<>() {
				@Override
				public Holder<T> asHolder() {
					return fakeHolder;
				}

				@Override
				public ResourceKey<I> getResourceKey() {
					return resourceKey;
				}

				@Override
				public ResourceLocation getId() {
					return location;
				}

				@Override
				public I get() {
					return forgeObj.get();
				}
			};
		}
	}

	public static class ForgeCustomRegistrationProvider<T> implements RegistrationProvider<T> {
		private final ResourceKey<? extends Registry<T>> key;
		private final String namespace;
		private final RegistryBuilder<T> builder;
		private Supplier<IForgeRegistry<T>> registrySupplier;

		ForgeCustomRegistrationProvider(ResourceKey<? extends Registry<T>> key, String namespace, RegistryBuilder<T> builder) {
			this.key = key;
			this.namespace = namespace;
			this.builder = builder;
		}

		public void bind(Supplier<IForgeRegistry<T>> reg) {
			this.registrySupplier = reg;
		}

		private IForgeRegistry<T> forgeRegistry() {
			IForgeRegistry<T> reg = registrySupplier.get();
			if (reg == null) {
				throw new IllegalStateException("Forge custom registry not constructed yet for " + key.location());
			}
			return reg;
		}


		@Override
		public Registry<T> registry() {
			throw new UnsupportedOperationException("Forge custom registries are IForgeRegistry, not Mojang Registry");
		}

		@Override
		public T get(ResourceLocation id) {
			return forgeRegistry().getValue(id);
		}

		@Override
		public ResourceLocation getId(T value) {
			return forgeRegistry().getKey(value);
		}

		@Override
		public Collection<T> values() {
			return forgeRegistry().getValues();
		}

		@Override
		public <I extends T> RegistryObject<T, I> register(String id, Supplier<? extends I> supplier) {
			ResourceLocation loc = new ResourceLocation(namespace, id);

			return new RegistryObject<>() {
				@Override
				public Holder<T> asHolder() {
					throw new UnsupportedOperationException("Forge custom registries do not support Holder<T>");
				}

				@Override
				@SuppressWarnings("unchecked")
				public ResourceKey<I> getResourceKey() {
					return (ResourceKey<I>) ResourceKey.create(
						(ResourceKey<? extends Registry<I>>) key,
						loc
					);
				}

				@Override
				public ResourceLocation getId() {
					return loc;
				}

				@Override
				@SuppressWarnings("unchecked")
				public I get() {
					return (I) forgeRegistry().getValue(loc);
				}
			};
		}
	}

	public static class CustomRegistryEntry<T> {
		public final RegistryBuilder<T> builder;
		public final ForgeCustomRegistrationProvider<T> provider;

		public CustomRegistryEntry(RegistryBuilder<T> builder, ForgeCustomRegistrationProvider<T> provider) {
			this.builder = builder;
			this.provider = provider;
		}
	}

	@Override
	public <T> void registerDatapackRegistry(ResourceKey<Registry<T>> key, Codec<T> codec, Codec<T> networkCodec) {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

		bus.addListener((DataPackRegistryEvent.NewRegistry event) -> {
			if (networkCodec != null) {
				event.dataPackRegistry(key, codec, networkCodec);
			} else {
				event.dataPackRegistry(key, codec);
			}
		});
	}

	@Override
	public CreativeModeTab getESTab() {
		return CreativeModeTab.builder()
			.icon(() -> new ItemStack(ESItems.STARLIGHT_FLOWER.get()))
			.title(Component.translatable("itemGroup.eternal_starlight"))
			.displayItems((displayParameters, output) -> {
				for (ResourceKey<Item> entry : ESItems.REGISTERED_ITEMS) {
					Item item = BuiltInRegistries.ITEM.get(entry);
					if (item != null) {
						output.accept(item);

						if (item == ESItems.STARLIT_PAINTING.get()) {
							displayParameters.holders()
								.lookup(Registries.PAINTING_VARIANT)
								.ifPresent(registryLookup ->
									ESCreativeModeTabs.generatePresetPaintings(
										output,
										displayParameters.holders(),
										registryLookup,
										holder -> holder.is(ESTags.PaintingVariants.PLACEABLE)
									)
								);
						}
					}
				}
			})
			.build();
	}

	@Override
	public <T> EntityDataAttachment<T> createAttachment(ResourceLocation id, Supplier<T> defaultValue, boolean copyOnDeath, BiPredicate<T, T> shouldSync, BiConsumer<FriendlyByteBuf, T> writer, Function<FriendlyByteBuf, T> reader) {
		return new ForgeEntityDataAttachment<>(id, defaultValue, copyOnDeath, shouldSync, writer, reader);
	}

	@Override
	public boolean isShears(ItemStack stack) {
		return stack.canPerformAction(ToolActions.SHEARS_CARVE);
	}

	@Override
	public boolean isShield(ItemStack stack) {
		return stack.getItem() instanceof ShieldItem;
	}

	@Override
	public SpawnEggItem createSpawnEgg(Supplier<EntityType<? extends Mob>> type, int bg, int hl, Item.Properties properties) {
		return new ForgeSpawnEggItem(type, bg, hl, properties);
	}

	@Override
	public Item createMobBucket(Supplier<? extends EntityType<? extends Mob>> type, Fluid fluid, SoundEvent sound, Item.Properties props) {
		return new DeferredMobBucketItem(type, fluid, sound, props);
	}

	@Override
	public EtherFluid.Still createEtherFluid() {
		return new ForgeEtherFluid.Still();
	}

	@Override
	public EtherFluid.Flowing createFlowingEtherFluid() {
		return new ForgeEtherFluid.Flowing();
	}

	@Override
	public Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> getToolTillAction(UseOnContext context) {
		return Pair.of(
			c -> c.getItemInHand().canPerformAction(ToolActions.HOE_TILL),
			c -> {
				BlockPos pos = c.getClickedPos();
				Level level = c.getLevel();
				BlockState state = level.getBlockState(pos);
				state.use(level, c.getPlayer(), InteractionHand.MAIN_HAND, c.getHitResult());
			}
		);
	}

	@Override
	public void sendToClient(ServerPlayer player, ESPacket packet) {
		ESForgeNetworkHandler.sendToClient(player, packet);
	}

	@Override
	public void sendToTrackingClientsImpl(ServerLevel level, Entity entity, ESPacket packet) {
		ESForgeNetworkHandler.sendToTracking(level, entity, packet);
	}

	@Override
	public Attribute getReach() {
		return ForgeMod.ENTITY_REACH.get();
	}

	@Override
	public Attribute getBlockReach() {
		return ForgeMod.BLOCK_REACH.get();
	}

}
