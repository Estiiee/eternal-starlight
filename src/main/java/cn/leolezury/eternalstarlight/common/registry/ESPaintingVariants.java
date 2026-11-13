package cn.leolezury.eternalstarlight.common.registry;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ESPaintingVariants {

	public static final DeferredRegister<PaintingVariant> REGISTRY = DeferredRegister.create(ForgeRegistries.PAINTING_VARIANTS, EternalStarlight.ID);

	// Have to register the paintings in code for 1.20.1.

	// Seperated due to not being present in 'placeable' JSON tag list.
	public static final RegistryObject<PaintingVariant> ASBOLUTE_ZERO = REGISTRY.register("asbolute_zero", () -> new PaintingVariant(32, 32));
	public static final RegistryObject<PaintingVariant> ASBOLUTE_ZERO_SPECIAL = REGISTRY.register("asbolute_zer0_special", () -> new PaintingVariant(32, 32));

	// Seperated due to not being present in 'placeable' JSON tag list.
	public static final RegistryObject<PaintingVariant> ENERGIZED = REGISTRY.register("energized", () -> new PaintingVariant(32, 32));
	public static final RegistryObject<PaintingVariant> ENERGIZED_SPECIAL = REGISTRY.register("energized_special", () -> new PaintingVariant(32, 32));

	// Seperated due to not being present in 'placeable' JSON tag list.
	public static final RegistryObject<PaintingVariant> MONSTROUS = REGISTRY.register("monstrous", () -> new PaintingVariant(32, 32));
	public static final RegistryObject<PaintingVariant> MONSTROUS_SPECIAL = REGISTRY.register("monstrous_special", () -> new PaintingVariant(32, 32));

	// Seperated due to not being present in 'placeable' JSON tag list.
	public static final RegistryObject<PaintingVariant> GUARDIAN = REGISTRY.register("guardian", () -> new PaintingVariant(32, 32));
	public static final RegistryObject<PaintingVariant> GUARDIAN_SPECIAL = REGISTRY.register("guardian_special", () -> new PaintingVariant(32, 32));

	// Randomly selected paintings when using Starlit Painting.
	public static final RegistryObject<PaintingVariant> CARVED_SLATE = REGISTRY.register("carved_slate", () -> new PaintingVariant(32, 16));
	public static final RegistryObject<PaintingVariant> COLD_ANOMALY = REGISTRY.register("cold_anomaly", () -> new PaintingVariant(16, 16));
	public static final RegistryObject<PaintingVariant> COOLER = REGISTRY.register("cooler", () -> new PaintingVariant(16, 32));
	public static final RegistryObject<PaintingVariant> CRYSTALBORN = REGISTRY.register("crystalborn", () -> new PaintingVariant(32, 32));
	public static final RegistryObject<PaintingVariant> EXTINGUISHED_SUN = REGISTRY.register("extinguished_sun", () -> new PaintingVariant(32, 16));
	public static final RegistryObject<PaintingVariant> HYMN_OF_THE_RATS = REGISTRY.register("hymn_of_the_rats", () -> new PaintingVariant(32, 48));
	public static final RegistryObject<PaintingVariant> INSECT_SPECIMEN = REGISTRY.register("insect_specimen", () -> new PaintingVariant(32, 16));
	public static final RegistryObject<PaintingVariant> NORTHLAND_HUT = REGISTRY.register("northland_hut", () -> new PaintingVariant(48, 32));
	public static final RegistryObject<PaintingVariant> NOVUS_SOL = REGISTRY.register("novus_sol", () -> new PaintingVariant(32, 16));
	public static final RegistryObject<PaintingVariant> ORIGINAL_CRETEOR = REGISTRY.register("original_creteor", () -> new PaintingVariant(16, 32));
	public static final RegistryObject<PaintingVariant> POT_OF_CRESTS = REGISTRY.register("pot_of_crests", () -> new PaintingVariant(16, 16));
	public static final RegistryObject<PaintingVariant> POWER = REGISTRY.register("power", () -> new PaintingVariant(16, 32));
	public static final RegistryObject<PaintingVariant> PUNGENCY_FRUIT_FEAST = REGISTRY.register("pungency_fruit_feast", () -> new PaintingVariant(16, 32));
	public static final RegistryObject<PaintingVariant> RAT_HEAD = REGISTRY.register("rat_head", () -> new PaintingVariant(32, 16));
	public static final RegistryObject<PaintingVariant> RIVEN_WELKIN = REGISTRY.register("riven_welkin", () -> new PaintingVariant(48, 48));
	public static final RegistryObject<PaintingVariant> SKELETON_LONESTAR = REGISTRY.register("skeleton_lonestar", () -> new PaintingVariant(16, 16));
	public static final RegistryObject<PaintingVariant> STACK = REGISTRY.register("stack", () -> new PaintingVariant(32, 32));
	public static final RegistryObject<PaintingVariant> STARFLOWERS = REGISTRY.register("starflowers", () -> new PaintingVariant(16, 32));
	public static final RegistryObject<PaintingVariant> SWORD_OF_THE_LAKE = REGISTRY.register("sword_of_the_lake", () -> new PaintingVariant(16, 16));
	public static final RegistryObject<PaintingVariant> THE_CATALYST = REGISTRY.register("the_catalyst", () -> new PaintingVariant(32, 32));
	public static final RegistryObject<PaintingVariant> THIRSTY = REGISTRY.register("thirsty", () -> new PaintingVariant(16, 32));
	public static final RegistryObject<PaintingVariant> TWILIGHT_SQUID = REGISTRY.register("twilight_squid", () -> new PaintingVariant(16, 16));
	public static final RegistryObject<PaintingVariant> UMBROUS_ALCHEMIST = REGISTRY.register("umbrous_alchemist", () -> new PaintingVariant(32, 32));
	public static final RegistryObject<PaintingVariant> VOIDSTONE_APPLE = REGISTRY.register("voidstone_apple", () -> new PaintingVariant(32, 32));
}
