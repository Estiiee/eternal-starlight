package cn.leolezury.eternalstarlight.common.datagen.provider.advancement;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.critereon.WitnessWeatherTrigger;
import cn.leolezury.eternalstarlight.common.data.ESBiomes;
import cn.leolezury.eternalstarlight.common.data.ESDimensions;
import cn.leolezury.eternalstarlight.common.data.ESStructures;
import cn.leolezury.eternalstarlight.common.registry.*;
import cn.leolezury.eternalstarlight.common.util.ESTags;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;

import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;

import java.util.List;
import java.util.function.Consumer;

public class ESAdvancementGenerator implements AdvancementSubProvider {

	@Override
	public void generate(HolderLookup.Provider registries, Consumer<Advancement> consumer) {
		HolderLookup.RegistryLookup<Biome> biomes = registries.lookupOrThrow(Registries.BIOME);
		HolderLookup.RegistryLookup<Structure> structures = registries.lookupOrThrow(Registries.STRUCTURE);
		HolderLookup.RegistryLookup<Fluid> fluids = registries.lookupOrThrow(Registries.FLUID);

		Advancement root = Advancement.Builder.advancement()
			.display(
				ESBlocks.LUNAR_LOG.get(),
				Component.translatable("advancements." + EternalStarlight.ID + ".root.title"),
				Component.translatable("advancements." + EternalStarlight.ID + ".root.description"),
				EternalStarlight.id("textures/block/lunar_log.png"),
				FrameType.TASK,
				false, false, false
			)
			.addCriterion("in_dim",
				PlayerTrigger.TriggerInstance.located(
					LocationPredicate.inDimension(ESDimensions.STARLIGHT_KEY)
				)
			)
			.save(consumer, EternalStarlight.ID + ":root");

		Advancement challengeGatekeeper = Advancement.Builder.advancement()
			.parent(root)
			.display(
				Items.DIAMOND_SWORD,
				Component.translatable("advancements." + EternalStarlight.ID + ".challenge_gatekeeper.title"),
				Component.translatable("advancements." + EternalStarlight.ID + ".challenge_gatekeeper.description"),
				null,
				FrameType.TASK,
				true, true, false
			)
			.addCriterion("challenged",
				new PlayerTrigger.TriggerInstance(
					ESCriteriaTriggers.CHALLENGED_GATEKEEPER.getId(),
					ContextAwarePredicate.ANY
				)
			)
			.save(consumer, EternalStarlight.ID + ":challenge_gatekeeper");

		Advancement obtainOrbOfProphecy = addItemObtain(consumer, challengeGatekeeper, "obtain_orb_of_prophecy", ESItems.ORB_OF_PROPHECY.get());

		Advancement enterDim = Advancement.Builder.advancement().parent(obtainOrbOfProphecy).display(
				ESBlocks.LUNAR_LOG.get(),
				Component.translatable("advancements." + EternalStarlight.ID + ".enter_starlight.title"),
				Component.translatable("advancements." + EternalStarlight.ID + ".enter_starlight.description"),
				null,
				FrameType.TASK,
				true, true, false)
			.addCriterion("in_dim",
				PlayerTrigger.TriggerInstance.located(
					LocationPredicate.Builder.location().setDimension(ESDimensions.STARLIGHT_KEY).build()))
			.save(consumer, EternalStarlight.ID + ":enter_starlight");

		Advancement seekingEye = addItemObtain(consumer, enterDim, "obtain_seeking_eye", ESItems.SEEKING_EYE.get());

		Advancement scythe = addItemObtain(consumer, enterDim, "obtain_scythe", ESTags.Items.SCYTHES, ESItems.PETAL_SCYTHE.get());

		Advancement enterAbyss = addInBiome(consumer, enterDim, "enter_abyss", ESItems.ABYSSLATE.get(), biomes.getOrThrow(ESBiomes.THE_ABYSS));

		Advancement redVelvetumossFlower = addItemObtain(consumer, enterAbyss, "obtain_red_velvetumoss_flower", ESItems.RED_VELVETUMOSS_FLOWER.get());

		Advancement.Builder builder = Advancement.Builder.advancement()
			.parent(enterDim)
			.display(
				ESBlocks.ICICLE.get(),
				Component.translatable("advancements." + EternalStarlight.ID + ".under_permafrost_forest.title"),
				Component.translatable("advancements." + EternalStarlight.ID + ".under_permafrost_forest.description"),
				null,
				FrameType.TASK,
				true, true, false
			)
			.requirements(RequirementsStrategy.OR);

		builder.addCriterion(
			"in_starlight_permafrost_forest",
			PlayerTrigger.TriggerInstance.located(
				LocationPredicate.Builder.location()
					.setBiome(ESBiomes.STARLIGHT_PERMAFROST_FOREST)
					.setY(MinMaxBounds.Doubles.atMost(-5))
					.build()
			)
		);

		builder.addCriterion(
			"in_permafrost_peaks",
			PlayerTrigger.TriggerInstance.located(
				LocationPredicate.Builder.location()
					.setBiome(ESBiomes.PERMAFROST_PEAKS)
					.setY(MinMaxBounds.Doubles.atMost(-5))
					.build()
			)
		);

		Advancement underPermafrostForest = builder.save(consumer, EternalStarlight.ID + ":under_permafrost_forest");

		Advancement glaciteShard = addItemObtain(consumer, underPermafrostForest, "obtain_glacite_shard", ESItems.GLACITE_SHARD.get());

		Advancement glaciteArrow = addItemObtain(consumer, glaciteShard, "obtain_glacite_arrow", ESItems.GLACITE_ARROW.get());

		Advancement frozenBomb = addItemObtain(consumer, underPermafrostForest, "obtain_frozen_bomb", ESItems.FROZEN_BOMB.get());

		Advancement auroraDeerAntler = addItemObtain(consumer, underPermafrostForest, "obtain_aurora_deer_antler", ESItems.AURORA_DEER_ANTLER.get());

		Advancement enterCrystallizedDesert = addInBiome(consumer, enterDim, "enter_crystallized_desert", ESItems.BLUE_STARLIGHT_CRYSTAL_SHARD.get(), biomes.getOrThrow(ESBiomes.CRYSTALLIZED_DESERT));

		Advancement crinoaSeeds = addItemObtain(consumer, enterCrystallizedDesert, "obtain_crinoa_seeds", ESItems.CRINOA_SEEDS.get());

		Advancement toothOfHunger = addItemObtain(consumer, enterCrystallizedDesert, "obtain_tooth_of_hunger", ESItems.TOOTH_OF_HUNGER.get());

		Advancement daggerOfHunger = addItemObtain(consumer, toothOfHunger, "obtain_dagger_of_hunger", ESItems.DAGGER_OF_HUNGER.get());

		Advancement saturateDaggerOfHunger = Advancement.Builder.advancement()
			.parent(daggerOfHunger)
			.display(
				ESItems.DAGGER_OF_HUNGER.get(),
				Component.translatable("advancements." + EternalStarlight.ID + ".saturate_dagger_of_hunger.title"),
				Component.translatable("advancements." + EternalStarlight.ID + ".saturate_dagger_of_hunger.description"),
				null,
				FrameType.TASK,
				true, true, false
			)
			.addCriterion("saturate",
				new PlayerTrigger.TriggerInstance(
					ESCriteriaTriggers.SATURATE_DAGGER_OF_HUNGER.getId(),
					ContextAwarePredicate.ANY
				)
			)
			.save(consumer, EternalStarlight.ID + ":saturate_dagger_of_hunger");

		Advancement toothOfHungerBlocks = addItemObtain(consumer, toothOfHunger, "obtain_tooth_of_hunger_blocks", ESTags.Items.TOOTH_OF_HUNGER_BLOCKS, ESItems.CHISELED_TOOTH_OF_HUNGER_TILES.get());

		Advancement crystalbornCatalyst = addItemObtain(consumer, toothOfHunger, "obtain_crystalborn_catalyst", ESItems.CRYSTALBORN_CATALYST.get());

		Advancement summonGrimstoneGolem = addEntitySummon(consumer, enterCrystallizedDesert, "summon_grimstone_golem", ESEntities.GRIMSTONE_GOLEM.get(), ESItems.GRIMSTONE_BRICKS.get());

		Advancement throwGleechEgg = Advancement.Builder.advancement()
			.parent(enterCrystallizedDesert)
			.display(
				ESItems.GLEECH_EGG.get(),
				Component.translatable("advancements." + EternalStarlight.ID + ".throw_gleech_egg.title"),
				Component.translatable("advancements." + EternalStarlight.ID + ".throw_gleech_egg.description"),
				null,
				FrameType.TASK,
				true, true, false
			)
			.addCriterion("thrown",
				new PlayerTrigger.TriggerInstance(
					ESCriteriaTriggers.THROW_GLEECH_EGG.getId(),
					ContextAwarePredicate.ANY
				)
			)
			.save(consumer, EternalStarlight.ID + ":throw_gleech_egg");

		Advancement tameCrystallizedMoth = Advancement.Builder.advancement().parent(enterCrystallizedDesert).display(
				ESItems.SHIVERING_GEL.get(),
				Component.translatable("advancements." + EternalStarlight.ID + ".tame_crystallized_moth.title"),
				Component.translatable("advancements." + EternalStarlight.ID + ".tame_crystallized_moth.description"),
				null,
				FrameType.TASK,
				true, true, false)
			.addCriterion("tame", TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().of(ESEntities.CRYSTALLIZED_MOTH.get()).build()))
			.save(consumer, EternalStarlight.ID + ":tame_crystallized_moth");

		Advancement inEtherFluid = addInFluid(consumer, enterDim, "in_ether_fluid", ESItems.ETHER_BUCKET.get(), ESTags.Fluids.ETHER);

		Advancement forgottenNocturnalMillet = addItemObtain(consumer, inEtherFluid, "obtain_forgotten_nocturnal_millet", ESItems.FORGOTTEN_NOCTURNAL_MILLET.get());

		Advancement thioquartzShard = addItemObtain(consumer, inEtherFluid, "obtain_thioquartz_shard", ESItems.THIOQUARTZ_SHARD.get());

		Advancement alchemistMask = addItemObtain(consumer, thioquartzShard, "obtain_alchemist_mask", ESItems.ALCHEMIST_MASK.get());

		Advancement witnessMeteorShower = Advancement.Builder.advancement()
			.parent(enterDim)
			.display(
				ESItems.RAW_AETHERSENT.get(),
				Component.translatable("advancements." + EternalStarlight.ID + ".witness_meteor_shower.title"),
				Component.translatable("advancements." + EternalStarlight.ID + ".witness_meteor_shower.description"),
				null,
				FrameType.TASK,
				true, true, false
			)
			.addCriterion("witnessed",
				new WitnessWeatherTrigger.TriggerInstance(
					ContextAwarePredicate.ANY,
					ESCriteriaTriggers.WITNESS_WEATHER.getId()
				)
			)

			.save(consumer, EternalStarlight.ID + ":witness_meteor_shower");

		Advancement aethersentIngot = addItemObtain(consumer, witnessMeteorShower, "obtain_aethersent_ingot", ESItems.AETHERSENT_INGOT.get());

		Advancement summonAethersentGolem = addEntitySummon(consumer, aethersentIngot, "summon_aethersent_golem", ESEntities.AETHERSENT_GOLEM.get(), ESItems.STARFALL_LONGBOW.get());

		Advancement killCreteor = addEntityKill(consumer, witnessMeteorShower, "kill_creteor", ESEntities.CRETEOR.get(), ESItems.CRETEOR_HIDE.get());

		Advancement aetherstrikeRocket = addItemObtain(consumer, killCreteor, "obtain_aetherstrike_rocket", ESItems.AETHERSTRIKE_ROCKET.get());

		Advancement deepsilverIngot = addItemObtain(consumer, enterDim, "obtain_deepsilver_ingot", ESItems.DEEPSILVER_INGOT.get());

		Advancement numbnessEffect = Advancement.Builder.advancement().parent(deepsilverIngot).display(
				ESItems.SILVER_PUNGENCY_FRUIT.get(),
				Component.translatable("advancements." + EternalStarlight.ID + ".obtain_numbness_effect.title"),
				Component.translatable("advancements." + EternalStarlight.ID + ".obtain_numbness_effect.description"),
				null,
				FrameType.TASK,
				true, true, false)
			.addCriterion("obtain",
				EffectsChangedTrigger.TriggerInstance.hasEffects(
					MobEffectsPredicate.effects()
						.and(ESMobEffects.NUMBNESS.get())
				)
			)
			.save(consumer, EternalStarlight.ID + ":obtain_numbness_effect");

		Advancement starlitDiamond = addItemObtain(consumer, enterDim, "obtain_starlit_diamond", ESItems.STARLIT_DIAMOND.get());

		Advancement igniteTearBomb = Advancement.Builder.advancement()
			.parent(enterDim)
			.display(
				ESItems.TEAR_BOMB.get(),
				Component.translatable("advancements." + EternalStarlight.ID + ".ignite_tear_bomb.title"),
				Component.translatable("advancements." + EternalStarlight.ID + ".ignite_tear_bomb.description"),
				null,
				FrameType.TASK,
				true, true, false
			)
			.addCriterion("ignite",
				new PlayerTrigger.TriggerInstance(
					ESCriteriaTriggers.IGNITE_TEAR_BOMB.getId(),
					ContextAwarePredicate.ANY
				)
			)
			.save(consumer, EternalStarlight.ID + ":ignite_tear_bomb");

		Advancement witnessStranghoulHunt = Advancement.Builder.advancement()
			.parent(enterDim)
			.display(
				ESItems.MALARITE_SWORD.get(),
				Component.translatable("advancements." + EternalStarlight.ID + ".witness_stranghoul_hunt.title"),
				Component.translatable("advancements." + EternalStarlight.ID + ".witness_stranghoul_hunt.description"),
				null,
				FrameType.TASK,
				true, true, false
			)
			.addCriterion("witness",
				new PlayerTrigger.TriggerInstance(
					ESCriteriaTriggers.WITNESS_STRANGHOUL_HUNT.getId(),
					ContextAwarePredicate.ANY
				)
			)
			.save(consumer, EternalStarlight.ID + ":witness_stranghoul_hunt");

		Advancement hireStranghoul = Advancement.Builder.advancement()
			.parent(witnessStranghoulHunt)
			.display(
				ESItems.PUNGENCY_STEW.get(),
				Component.translatable("advancements." + EternalStarlight.ID + ".hire_stranghoul.title"),
				Component.translatable("advancements." + EternalStarlight.ID + ".hire_stranghoul.description"),
				null,
				FrameType.TASK,
				true, true, false
			)
			.addCriterion("hire",
				new PlayerTrigger.TriggerInstance(
					ESCriteriaTriggers.HIRE_STRANGHOUL.getId(),
					ContextAwarePredicate.ANY
				)
			)
			.save(consumer, EternalStarlight.ID + ":hire_stranghoul");

		Advancement hammerCriticalHit = Advancement.Builder.advancement()
			.parent(enterDim)
			.display(
				ESItems.STARFIRE_HAMMER.get(),
				Component.translatable("advancements." + EternalStarlight.ID + ".hammer_critical_hit.title"),
				Component.translatable("advancements." + EternalStarlight.ID + ".hammer_critical_hit.description"),
				null,
				FrameType.TASK,
				true, true, false
			)
			.addCriterion("critical_hit",
				new PlayerTrigger.TriggerInstance(
					ESCriteriaTriggers.HAMMER_CRITICAL_HIT.getId(),
					ContextAwarePredicate.ANY
				)
			)
			.save(consumer, EternalStarlight.ID + ":hammer_critical_hit");

		Advancement putSeedsIntoStarfireBirdNest = Advancement.Builder.advancement()
			.parent(enterDim)
			.display(
				ESItems.STARFIRE_BIRD_NEST.get(),
				Component.translatable("advancements." + EternalStarlight.ID + ".put_seeds_into_starfire_bird_nest.title"),
				Component.translatable("advancements." + EternalStarlight.ID + ".put_seeds_into_starfire_bird_nest.description"),
				null,
				FrameType.TASK,
				true, true, false
			)
			.addCriterion("put_seeds",
				new PlayerTrigger.TriggerInstance(
					ESCriteriaTriggers.PUT_SEEDS_INTO_STARFIRE_BIRD_NEST.getId(),
					ContextAwarePredicate.ANY
				)
			)
			.save(consumer, EternalStarlight.ID + ":put_seeds_into_starfire_bird_nest");


		Advancement starfire = addItemObtain(consumer, putSeedsIntoStarfireBirdNest, "obtain_starfire", ESItems.STARFIRE.get());

		Advancement flowglaze = addItemObtain(consumer, starfire, "obtain_flowglaze", ESItems.FLOWGLAZE.get());

		Advancement thermalSpringstone = addItemObtain(consumer, enterDim, "obtain_thermal_springstone", ESItems.THERMAL_SPRINGSTONE.get());

		Advancement rawAmaramber = addItemObtain(consumer, enterDim, "obtain_raw_amaramber", ESItems.RAW_AMARAMBER.get());

		Advancement stellagmite = addItemObtain(consumer, enterDim, "obtain_stellagmite", ESItems.STELLAGMITE.get());

		Advancement bouldershroomStew = addItemObtain(consumer, enterDim, "obtain_bouldershroom_stew", ESItems.BOULDERSHROOM_STEW.get());

		Advancement dustedShard = addItemObtain(consumer, enterDim, "obtain_dusted_shard", ESItems.DUSTED_SHARD.get());

		Advancement jinglestemSandwich = addItemObtain(consumer, enterDim, "obtain_jinglestem_sandwich", ESItems.JINGLESTEM_SANDWICH.get());

		Advancement.Builder allStarlightBiomesBuilder = Advancement.Builder.advancement().parent(enterDim).display(
				ESBlocks.NIGHTFALL_GRASS_BLOCK.get(),
				Component.translatable("advancements." + EternalStarlight.ID + ".all_starlight_biomes.title"),
				Component.translatable("advancements." + EternalStarlight.ID + ".all_starlight_biomes.description"),
				null,
				FrameType.CHALLENGE,
				true, true, false)
			.requirements(RequirementsStrategy.AND)
			.rewards(AdvancementRewards.Builder.experience(500));
		List<ResourceKey<Biome>> biomeIds = biomes.listElementIds().sorted(ResourceKey::compareTo).toList();
		for (ResourceKey<Biome> key : biomeIds) {
			if (key.location().getNamespace().equals(EternalStarlight.ID)) {
				allStarlightBiomesBuilder.addCriterion(
					"in_" + key.location().getPath(),
					PlayerTrigger.TriggerInstance.located(
						LocationPredicate.inBiome(biomes.getOrThrow(key).key())
					)
				);
			}

		}
		Advancement allStarlightBiomes = allStarlightBiomesBuilder.save(consumer, EternalStarlight.ID + ":all_starlight_biomes");

		Advancement enterGolemForge = addInStructure(consumer, seekingEye, "enter_golem_forge", ESItems.GOLEM_STEEL_PILLAR.get(), structures.getOrThrow(ESStructures.GOLEM_FORGE));

		Advancement killPermafrost = addEntityKill(consumer, enterGolemForge, "kill_permafrost", ESEntities.PERMAFROST.get(), ESItems.COLDSNAP.get());

		Advancement freezeStarlightGolem = Advancement.Builder.advancement()
			.parent(enterGolemForge)
			.display(
				ESItems.FROZEN_TUBE.get(),
				Component.translatable("advancements." + EternalStarlight.ID + ".freeze_starlight_golem.title"),
				Component.translatable("advancements." + EternalStarlight.ID + ".freeze_starlight_golem.description"),
				null,
				FrameType.TASK,
				true, true, false
			)
			.addCriterion("freeze",
				new PlayerTrigger.TriggerInstance(
					ESCriteriaTriggers.FREEZE_STARLIGHT_GOLEM.getId(),
					ContextAwarePredicate.ANY
				)
			)
			.save(consumer, EternalStarlight.ID + ":freeze_starlight_golem");

		Advancement deactivateEnergyBlock = Advancement.Builder.advancement()
			.parent(freezeStarlightGolem)
			.display(
				ESItems.ENERGY_BLOCK.get(),
				Component.translatable("advancements." + EternalStarlight.ID + ".deactivate_energy_block.title"),
				Component.translatable("advancements." + EternalStarlight.ID + ".deactivate_energy_block.description"),
				null,
				FrameType.TASK,
				true, true, false
			)
			.addCriterion("deactivate",
				new PlayerTrigger.TriggerInstance(
					ESCriteriaTriggers.DEACTIVATE_ENERGY_BLOCK.getId(),
					ContextAwarePredicate.ANY
				)
			)
			.save(consumer, EternalStarlight.ID + ":deactivate_energy_block");

		Advancement killGolem = addEntityKill(consumer, deactivateEnergyBlock, "kill_golem", ESEntities.STARLIGHT_GOLEM.get(), ESItems.CHISELED_GOLEM_STEEL_BLOCK.get());

		Advancement golemSteelIngot = addItemObtain(consumer, killGolem, "obtain_golem_steel_ingot", ESItems.GOLEM_STEEL_INGOT.get());

		Advancement igniteLunarMonstrosity = Advancement.Builder.advancement().parent(killGolem).display(
				ESItems.SALTPETER_MATCHBOX.get(),
				Component.translatable("advancements." + EternalStarlight.ID + ".ignite_lunar_monstrosity.title"),
				Component.translatable("advancements." + EternalStarlight.ID + ".ignite_lunar_monstrosity.description"),
				null,
				FrameType.TASK,
				true, true, false)
			.addCriterion("ignite", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(ItemPredicate.Builder.item().of(ESTags.Items.LUNAR_MONSTROSITY_IGNITERS), ContextAwarePredicate.ANY))
			.save(consumer, EternalStarlight.ID + ":ignite_lunar_monstrosity");

		Advancement killLunarMonstrosity = addEntityKill(consumer, igniteLunarMonstrosity, "kill_lunar_monstrosity", ESEntities.LUNAR_MONSTROSITY.get(), ESItems.PARASOL_GRASS.get());

		Advancement crescentSpear = addItemObtain(consumer, killLunarMonstrosity, "obtain_crescent_spear", ESItems.CRESCENT_SPEAR.get());

		Advancement chainTangledSkullExplosion = Advancement.Builder.advancement()
			.parent(killLunarMonstrosity)
			.display(
				ESItems.TANGLED_SKULL.get(),
				Component.translatable("advancements." + EternalStarlight.ID + ".chain_tangled_skull_explosion.title"),
				Component.translatable("advancements." + EternalStarlight.ID + ".chain_tangled_skull_explosion.description"),
				null,
				FrameType.CHALLENGE,
				true, true, false
			)
			.rewards(AdvancementRewards.Builder.experience(60))
			.addCriterion("explode",
				new PlayerTrigger.TriggerInstance(
					ESCriteriaTriggers.CHAIN_TANGLED_SKULL_EXPLOSION.getId(),
					ContextAwarePredicate.ANY
				)
			)
			.save(consumer, EternalStarlight.ID + ":chain_tangled_skull_explosion");

		Advancement.Builder.advancement().parent(killLunarMonstrosity).display(
				ESItems.UNREALIUM_HELMET.get(),
				Component.translatable("advancements." + EternalStarlight.ID + ".full_unrealium_armor.title"),
				Component.translatable("advancements." + EternalStarlight.ID + ".full_unrealium_armor.description"),
				null, FrameType.CHALLENGE, true, true, false)
			.rewards(AdvancementRewards.Builder.experience(100))
			.addCriterion("has_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(ESItems.UNREALIUM_HELMET.get()))
			.addCriterion("has_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(ESItems.UNREALIUM_CHESTPLATE.get()))
			.addCriterion("has_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(ESItems.UNREALIUM_LEGGINGS.get()))
			.addCriterion("has_boots", InventoryChangeTrigger.TriggerInstance.hasItems(ESItems.UNREALIUM_BOOTS.get()))
			.save(consumer, EternalStarlight.ID + ":full_unrealium_armor");

		Advancement useBlossomOfStars = Advancement.Builder.advancement().parent(enterDim).display(
				ESItems.BLOSSOM_OF_STARS.get(),
				Component.translatable("advancements." + EternalStarlight.ID + ".use_blossom_of_stars.title"),
				Component.translatable("advancements." + EternalStarlight.ID + ".use_blossom_of_stars.description"),
				null,
				FrameType.CHALLENGE,
				true, true, true)
			.addCriterion("use_item",
				ConsumeItemTrigger.TriggerInstance.usedItem(ESItems.BLOSSOM_OF_STARS.get()))
			.save(consumer, EternalStarlight.ID + ":use_blossom_of_stars");
	}

	private static Advancement addItemObtain(Consumer<Advancement> consumer, Advancement parent, String id, Item item) {
		return Advancement.Builder.advancement().parent(parent).display(
				item,
				Component.translatable("advancements." + EternalStarlight.ID + "." + id + ".title"),
				Component.translatable("advancements." + EternalStarlight.ID + "." + id + ".description"),
				null, FrameType.TASK, true, true, false)
			.addCriterion("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(item))
			.save(consumer, EternalStarlight.ID + ":" + id);
	}

	private static Advancement addItemObtain(Consumer<Advancement> consumer, Advancement parent, String id, TagKey<Item> tag, Item item) {
		return Advancement.Builder.advancement()
			.parent(parent)
			.display(
				item,
				Component.translatable("advancements." + EternalStarlight.ID + "." + id + ".title"),
				Component.translatable("advancements." + EternalStarlight.ID + "." + id + ".description"),
				null,
				FrameType.TASK,
				true, true, false
			)
			.addCriterion("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(tag).build()))
			.save(consumer, EternalStarlight.ID + ":" + id);
	}


	private static Advancement addEntityKill(Consumer<Advancement> consumer, Advancement parent, String id, EntityType<?> entity, Item item) {
		return addEntityKill(consumer, parent, id, EntityPredicate.Builder.entity().of(entity).build(), item);
	}

	private static Advancement addEntityKill(Consumer<Advancement> consumer, Advancement parent, String id, EntityPredicate predicate, Item item) {
		return Advancement.Builder.advancement()
			.parent(parent)
			.display(
				item,
				Component.translatable("advancements." + EternalStarlight.ID + "." + id + ".title"),
				Component.translatable("advancements." + EternalStarlight.ID + "." + id + ".description"),
				null,
				FrameType.TASK,
				true, true, false
			)
			.addCriterion("kill", KilledTrigger.TriggerInstance.playerKilledEntity(predicate != null ? predicate : EntityPredicate.ANY))
			.save(consumer, EternalStarlight.ID + ":" + id);
	}

	private static Advancement addEntitySummon(Consumer<Advancement> consumer, Advancement parent, String id, EntityType<?> entity, Item item) {
		return addEntitySummon(consumer, parent, id, EntityPredicate.Builder.entity().of(entity), item);
	}

	private static Advancement addEntitySummon(Consumer<Advancement> consumer, Advancement parent, String id, EntityPredicate.Builder predicate, Item item) {
		return Advancement.Builder.advancement().parent(parent).display(
				item,
				Component.translatable("advancements." + EternalStarlight.ID + "." + id + ".title"),
				Component.translatable("advancements." + EternalStarlight.ID + "." + id + ".description"),
				null, FrameType.TASK, true, true, false)
			.addCriterion("summon", SummonedEntityTrigger.TriggerInstance.summonedEntity(predicate))
			.save(consumer, EternalStarlight.ID + ":" + id);
	}

	private static Advancement addInBiome(Consumer<Advancement> consumer, Advancement parent, String id, Item display, Holder<Biome> biome) {
		return Advancement.Builder.advancement()
			.parent(parent)
			.display(
				display,
				Component.translatable("advancements." + EternalStarlight.ID + "." + id + ".title"),
				Component.translatable("advancements." + EternalStarlight.ID + "." + id + ".description"),
				null,
				FrameType.TASK,
				true, true, false
			)
			.addCriterion(
				"in_biome",
				PlayerTrigger.TriggerInstance.located(
					LocationPredicate.inBiome(biome.unwrapKey().orElseThrow())
				)
			)
			.save(consumer, EternalStarlight.ID + ":" + id);
	}

	private static Advancement addInStructure(Consumer<Advancement> consumer, Advancement parent, String id, Item display, Holder<Structure> structure) {
		return Advancement.Builder.advancement()
			.parent(parent)
			.display(
				display,
				Component.translatable("advancements." + EternalStarlight.ID + "." + id + ".title"),
				Component.translatable("advancements." + EternalStarlight.ID + "." + id + ".description"),
				null,
				FrameType.TASK,
				true, true, false
			)
			.addCriterion("in_structure", PlayerTrigger.TriggerInstance.located(LocationPredicate.inStructure(structure.unwrapKey().orElseThrow())))
			.save(consumer, EternalStarlight.ID + ":" + id);
	}

	private static Advancement addInFluid(Consumer<Advancement> consumer, Advancement parent, String id, Item display, TagKey<Fluid> fluidTag) {
		return Advancement.Builder.advancement()
			.parent(parent)
			.display(
				display,
				Component.translatable("advancements." + EternalStarlight.ID + "." + id + ".title"),
				Component.translatable("advancements." + EternalStarlight.ID + "." + id + ".description"),
				null,
				FrameType.TASK,
				true, true, false
			)
			.addCriterion(
				"in_fluid",
				PlayerTrigger.TriggerInstance.located(
					LocationPredicate.Builder.location()
						.setFluid(FluidPredicate.Builder.fluid().of(fluidTag).build())
						.build()
				)
			)
			.save(consumer, EternalStarlight.ID + ":" + id);
	}

}
