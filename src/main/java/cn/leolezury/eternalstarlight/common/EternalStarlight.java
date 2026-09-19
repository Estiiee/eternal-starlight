package cn.leolezury.eternalstarlight.common;

import cn.leolezury.eternalstarlight.common.block.flammable.ESFlammabilityRegistry;
import cn.leolezury.eternalstarlight.common.client.helper.ClientHelper;
import cn.leolezury.eternalstarlight.common.client.helper.ClientSideHelper;
import cn.leolezury.eternalstarlight.common.config.ESConfig;
import cn.leolezury.eternalstarlight.common.data.ESEnchantments;
import cn.leolezury.eternalstarlight.common.data.ESRegistries;
import cn.leolezury.eternalstarlight.common.handler.ESCommonSetupHandler;
import cn.leolezury.eternalstarlight.common.network.ESPackets;
import cn.leolezury.eternalstarlight.common.network.handler.ESForgeNetworkHandler;
import cn.leolezury.eternalstarlight.common.platform.ESForgePlatform;
import cn.leolezury.eternalstarlight.common.registry.*;
import cn.leolezury.eternalstarlight.common.util.ESMiscUtil;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Mod(EternalStarlight.ID)
public class EternalStarlight {
	public static final String ID = "eternal_starlight";
	public static final Logger LOGGER = LogUtils.getLogger();

	public EternalStarlight(FMLJavaModLoadingContext context) {
		IEventBus modBus = context.getModEventBus();
		ESForgePlatform.init(modBus);

		ESFluidTypes.loadClass();
		ESPackets.init();
		//ESConfig.load();
		context.registerConfig(ModConfig.Type.COMMON, ESConfig.SPEC);
		ESFluids.loadClass();
		ESBlocks.loadClass();
		ESPoiTypes.loadClass();
		ESArmorMaterials.loadClass();
		ESSpells.loadClass();
		ESEntities.loadClass();
		ESPotions.loadClass();
		ESItems.loadClass();
		ESCreativeModeTabs.loadClass();
		ESCriteriaTriggers.loadClass();
		ESAttributes.loadClass();
		ESSensorTypes.loadClass();
		ESMobEffects.loadClass();
		ESBlockEntities.loadClass();
		ESMenuTypes.loadClass();
		ESMaterialConditions.loadClass();
		ESWorldCarvers.loadClass();
		ESFeatures.loadClass();
		ESPlacementModifierTypes.loadClass();
		ESTreePlacers.loadClass();
		ESTreeDecorators.loadClass();
		ESStructureTypes.loadClass();
		ESStructurePieceTypes.loadClass();
		ESStructurePlacementTypes.loadClass();
		ESStructurePoolElementTypes.loadClass();
		ESParticles.loadClass();
		ESSoundEvents.loadClass();
		ESRecipeSerializers.loadClass();
		ESRecipes.loadClass();
		ESWeathers.loadClass();
		ESBoarwarfProfessions.loadClass();
		ESDataAttachments.loadClass();
		ESRegistries.loadClass();
		ESFlammabilityRegistry.registerDefaults();
		ESEnchantments.loadClass();
		ESForgeNetworkHandler.register();

		modBus.addListener(this::onCommonSetup);
		modBus.addListener(this::onRegister);
		modBus.addListener(this::onNewRegistry);
		for (DeferredRegister<?> register : ESForgePlatform.REGISTERS) {
			register.register(modBus);
		}
		//ESForgePlatform.ATTACHMENT_TYPE_REGISTER.register(modBus);
		//ESRegistryRemapper.addAliases();
	}

	public static ResourceLocation id(String string) {
		return new ResourceLocation(EternalStarlight.ID, string);
	}

	public static ClientHelper getClientHelper() {
		AtomicReference<ClientHelper> helper = new AtomicReference<>(new ClientHelper());
		ESMiscUtil.runWhenOnClient(() -> () -> helper.set(new ClientSideHelper()));
		return helper.get();
	}

	private void onCommonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
		});
	}

	private void onRegister(RegisterEvent event) {
		if (event.getRegistryKey().equals(Registries.CHUNK_GENERATOR)) {
			ESCommonSetupHandler.registerChunkGenerator();
		} else if (event.getRegistryKey().equals(Registries.BIOME_SOURCE)) {
			ESCommonSetupHandler.registerBiomeSource();
		}
	}

	private void onNewRegistry(NewRegistryEvent event) {
		for (ESForgePlatform.CustomRegistryEntry<?> entry : ESForgePlatform.CUSTOM_REGISTRIES) {
			bindEntry(event, entry);
		}
	}

	private <T> void bindEntry(NewRegistryEvent event, ESForgePlatform.CustomRegistryEntry<T> entry) {
		Supplier<IForgeRegistry<T>> sup = event.create(entry.builder);
		entry.provider.bind(sup);
	}
}