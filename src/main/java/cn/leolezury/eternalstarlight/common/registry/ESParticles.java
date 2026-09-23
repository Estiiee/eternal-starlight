package cn.leolezury.eternalstarlight.common.registry;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.particle.*;
import cn.leolezury.eternalstarlight.common.platform.registry.RegistrationProvider;
import cn.leolezury.eternalstarlight.common.platform.registry.RegistryObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

public class ESParticles {

	public static final RegistrationProvider<ParticleType<?>> PARTICLE_TYPES =
		RegistrationProvider.get(Registries.PARTICLE_TYPE, EternalStarlight.ID);

	public static final RegistryObject<ParticleType<?>, SimpleParticleType> STARLIGHT = PARTICLE_TYPES.register("starlight", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> STARDUST = PARTICLE_TYPES.register("stardust", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> FIREFLY = PARTICLE_TYPES.register("firefly", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> SCARLET_LEAVES = PARTICLE_TYPES.register("scarlet_leaves", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> CRADLEWOOD_LEAVES = PARTICLE_TYPES.register("cradlewood_leaves", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> SHADEGRIEVE_LEAVES = PARTICLE_TYPES.register("shadegrieve_leaves", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> SPIRAL_KELP_LEAVES = PARTICLE_TYPES.register("spiral_kelp_leaves", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> FALLING_RED_CRYSTAL_MOSS = PARTICLE_TYPES.register("falling_red_crystal_moss", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> FALLING_BLUE_CRYSTAL_MOSS = PARTICLE_TYPES.register("falling_blue_crystal_moss", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> ENERGY = PARTICLE_TYPES.register("energy", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> ELECTRIC_SPARK = PARTICLE_TYPES.register("electric_spark", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> LUNAR_SLASH = PARTICLE_TYPES.register("lunar_slash", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> CRYSTALLIZED_MOTH_SONAR = PARTICLE_TYPES.register("crystallized_moth_sonar", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> AMARAMBER_FLAME = PARTICLE_TYPES.register("amaramber_flame", () -> new SimpleParticleType(false));

	public static final RegistryObject<ParticleType<?>, ParticleType<ESExplosionParticleOptions>> EXPLOSION =
		PARTICLE_TYPES.register("explosion", () ->
			new ParticleType<>(false, ESExplosionParticleOptions.DESERIALIZER) {
				@Override
				public Codec<ESExplosionParticleOptions> codec() {
					return ESExplosionParticleOptions.CODEC;
				}
			});

	public static final RegistryObject<ParticleType<?>, ParticleType<ESExplosionParticleOptions>> BLAST =
		PARTICLE_TYPES.register("blast", () ->
			new ParticleType<>(false, ESExplosionParticleOptions.DESERIALIZER) {
				@Override
				public Codec<ESExplosionParticleOptions> codec() {
					return ESExplosionParticleOptions.CODEC;
				}
			});

	public static final RegistryObject<ParticleType<?>, ParticleType<ESSmokeParticleOptions>> SMOKE =
		PARTICLE_TYPES.register("smoke", () ->
			new ParticleType<>(false, ESSmokeParticleOptions.DESERIALIZER) {
				@Override
				public Codec<ESSmokeParticleOptions> codec() {
					return ESSmokeParticleOptions.CODEC.codec();
				}
			});

	public static final RegistryObject<ParticleType<?>, ParticleType<RingExplosionParticleOptions>> RING_EXPLOSION =
		PARTICLE_TYPES.register("ring_explosion", () ->
			new ParticleType<>(false, RingExplosionParticleOptions.DESERIALIZER) {
				@Override
				public Codec<RingExplosionParticleOptions> codec() {
					return RingExplosionParticleOptions.CODEC.codec();
				}
			});

	public static final RegistryObject<ParticleType<?>, ParticleType<RingParticleOptions>> RING =
		PARTICLE_TYPES.register("ring", () ->
			new ParticleType<>(false, RingParticleOptions.DESERIALIZER) {
				@Override
				public Codec<RingParticleOptions> codec() {
					return RingParticleOptions.CODEC.codec();
				}
			});

	public static final RegistryObject<ParticleType<?>, ParticleType<SurroundingTrailParticleOptions>> SURROUNDING_TRAIL  =
		PARTICLE_TYPES.register("surrounding_trail", () ->
			new ParticleType<>(false, SurroundingTrailParticleOptions.DESERIALIZER) {
				@Override
				public Codec<SurroundingTrailParticleOptions> codec() {
					return SurroundingTrailParticleOptions.CODEC.codec();
				}
			});

	public static final RegistryObject<ParticleType<?>, ParticleType<GatheringTrailParticleOptions>> GATHERING_ENERGY =
		PARTICLE_TYPES.register("gathering_energy", () ->
			new ParticleType<>(false, GatheringTrailParticleOptions.DESERIALIZER) {
				@Override
				public Codec<GatheringTrailParticleOptions> codec() {
					return GatheringTrailParticleOptions.codec(ESParticles.GATHERING_ENERGY.get()).codec();
				}
			});

	public static final RegistryObject<ParticleType<?>, ParticleType<GatheringTrailParticleOptions>> GATHERING_SOUL =
		PARTICLE_TYPES.register("gathering_soul", () ->
			new ParticleType<>(false, GatheringTrailParticleOptions.DESERIALIZER) {
				@Override
				public Codec<GatheringTrailParticleOptions> codec() {
					return GatheringTrailParticleOptions.codec(ESParticles.GATHERING_SOUL.get()).codec();
				}
			});

	public static final RegistryObject<ParticleType<?>, ParticleType<GatheringTrailParticleOptions>> GATHERING_FLARE =
		PARTICLE_TYPES.register("gathering_flare", () ->
			new ParticleType<>(false, GatheringTrailParticleOptions.DESERIALIZER) {
				@Override
				public Codec<GatheringTrailParticleOptions> codec() {
					return GatheringTrailParticleOptions.codec(ESParticles.GATHERING_FLARE.get()).codec();
				}
			});

	public static final RegistryObject<ParticleType<?>, ParticleType<ESGlowParticleOptions>> GLOW =
		PARTICLE_TYPES.register("glow", () ->
			new ParticleType<>(false, ESGlowParticleOptions.DESERIALIZER) {
				@Override
				public Codec<ESGlowParticleOptions> codec() {
					return ESGlowParticleOptions.CODEC.codec();
				}
			});

	public static final RegistryObject<ParticleType<?>, ParticleType<ExplosionShockParticleOptions>> EXPLOSION_SHOCK =
		PARTICLE_TYPES.register("explosion_shock", () ->
			new ParticleType<>(false, ExplosionShockParticleOptions.DESERIALIZER) {
				@Override
				public Codec<ExplosionShockParticleOptions> codec() {
					return ExplosionShockParticleOptions.CODEC.codec();
				}
			});

	public static final RegistryObject<ParticleType<?>, ParticleType<ForgeColorParticleOption>> COLORED_INK =
		PARTICLE_TYPES.register("colored_ink", () ->
			new ParticleType<>(false, ForgeColorParticleOption.DESERIALIZER) {
				@Override
				public Codec<ForgeColorParticleOption> codec() {
					return ForgeColorParticleOption.codec(ESParticles.COLORED_INK.get()).codec();
				}
			});

	public static final RegistryObject<ParticleType<?>, ParticleType<GeyserParticleOptions>> GEYSER =
		PARTICLE_TYPES.register("geyser", () ->
			new ParticleType<>(true, GeyserParticleOptions.DESERIALIZER) {
				@Override
				public Codec<GeyserParticleOptions> codec() {
					return GeyserParticleOptions.codec(ESParticles.GEYSER.get()).codec();
				}
			});

	public static final RegistryObject<ParticleType<?>, ParticleType<GeyserBaseParticleOptions>> GEYSER_BASE =
		PARTICLE_TYPES.register("geyser_base", () ->
			new ParticleType<>(true, GeyserBaseParticleOptions.DESERIALIZER) {
				@Override
				public Codec<GeyserBaseParticleOptions> codec() {
					return GeyserBaseParticleOptions.codec(ESParticles.GEYSER_BASE.get()).codec();
				}
			});

	public static final RegistryObject<ParticleType<?>, ParticleType<GeyserBaseParticleOptions>> GEYSER_POOF =
		PARTICLE_TYPES.register("geyser_poof", () ->
			new ParticleType<>(true, GeyserBaseParticleOptions.DESERIALIZER) {
				@Override
				public Codec<GeyserBaseParticleOptions> codec() {
					return GeyserBaseParticleOptions.codec(ESParticles.GEYSER_POOF.get()).codec();
				}
			});

	public static final RegistryObject<ParticleType<?>, ParticleType<GeyserParticleOptions>> GEYSER_PLUME =
		PARTICLE_TYPES.register("geyser_plume", () ->
			new ParticleType<>(true, GeyserParticleOptions.DESERIALIZER) {
				@Override
				public Codec<GeyserParticleOptions> codec() {
					return GeyserParticleOptions.codec(ESParticles.GEYSER_PLUME.get()).codec();
				}
			});

	public static final RegistryObject<ParticleType<?>, ParticleType<OrbitalTrailParticleOptions>> ORBITAL_FLARE =
		PARTICLE_TYPES.register("orbital_flare", () ->
			new ParticleType<>(false, OrbitalTrailParticleOptions.DESERIALIZER) {
				@Override
				public Codec<OrbitalTrailParticleOptions> codec() {
					return OrbitalTrailParticleOptions.codec(ESParticles.ORBITAL_FLARE.get()).codec();
				}
			});

	public static final RegistryObject<ParticleType<?>, ParticleType<RippleParticleOptions>> RIPPLE  =
		PARTICLE_TYPES.register("ripple", () ->
			new ParticleType<>(false, RippleParticleOptions.DESERIALIZER) {
				@Override
				public Codec<RippleParticleOptions> codec() {
					return RippleParticleOptions.codec(ESParticles.RIPPLE.get()).codec();
				}
			});

	public static final RegistryObject<ParticleType<?>, SimpleParticleType> METEOR = PARTICLE_TYPES.register("meteor", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> PARRY = PARTICLE_TYPES.register("parry", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> AETHERSENT_SMOKE = PARTICLE_TYPES.register("aethersent_smoke", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> SMOKE_TRAIL = PARTICLE_TYPES.register("smoke_trail", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> AETHERSENT_EXPLOSION = PARTICLE_TYPES.register("aethersent_explosion", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> ASHEN_SNOW = PARTICLE_TYPES.register("ashen_snow", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> ORBITAL_ASHEN_SNOW = PARTICLE_TYPES.register("orbital_ashen_snow", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> AMARAMBER_WAX_ON = PARTICLE_TYPES.register("amaramber_wax_on", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> DRIPPING_MUD = PARTICLE_TYPES.register("dripping_mud", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> FALLING_MUD = PARTICLE_TYPES.register("falling_mud", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> LANDING_MUD = PARTICLE_TYPES.register("landing_mud", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> ALLIED = PARTICLE_TYPES.register("allied", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> PUNGENCY_FRUIT_SMOKE = PARTICLE_TYPES.register("pungency_fruit_smoke", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> STARFIRE = PARTICLE_TYPES.register("starfire", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> STARFIRE_EXPLOSION = PARTICLE_TYPES.register("starfire_explosion", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> STARFIRE_EXPLOSION_SMALL = PARTICLE_TYPES.register("starfire_explosion_small", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> SOUL_TRAIL = PARTICLE_TYPES.register("soul_trail", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> CANDLASH_TRAIL = PARTICLE_TYPES.register("candlash_trail", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> ETHER_TRAIL = PARTICLE_TYPES.register("ether_trail", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> ADVANCED_GLOW = PARTICLE_TYPES.register("advanced_glow", () -> new SimpleParticleType(false));
	public static final RegistryObject<ParticleType<?>, SimpleParticleType> SHINE = PARTICLE_TYPES.register("shine", () -> new SimpleParticleType(false));

	public static void loadClass() {}
}
