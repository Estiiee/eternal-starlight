package cn.leolezury.eternalstarlight.common.particle;

import cn.leolezury.eternalstarlight.common.registry.ESParticles;
import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import org.joml.Vector3f;

public record RingExplosionParticleOptions(Vector3f fromColor, Vector3f toColor, float scale) implements ParticleOptions {
	public static final RingExplosionParticleOptions SHOCKWAVE = fromIntColor(new Vector3f(255, 255, 255), new Vector3f(255, 255, 255), 4);
	public static final RingExplosionParticleOptions ENERGY = fromIntColor(new Vector3f(0, 234, 255), new Vector3f(255, 255, 255), 4);
	public static final RingExplosionParticleOptions ENERGY_SMALL = fromIntColor(new Vector3f(0, 234, 255), new Vector3f(255, 255, 255), 1.5f);
	public static final RingExplosionParticleOptions LUNAR = fromIntColor(new Vector3f(66, 66, 115), new Vector3f(32, 32, 64), 10);
	public static final RingExplosionParticleOptions SOUL = fromIntColor(new Vector3f(96, 245, 250), new Vector3f(131, 140, 141), 3);
	public static final RingExplosionParticleOptions FLARE = fromIntColor(new Vector3f(222, 112, 255), new Vector3f(255, 255, 116), 4);

	public static RingExplosionParticleOptions fromIntColor(Vector3f fromColor, Vector3f toColor, float scale) {
		return new RingExplosionParticleOptions(
			new Vector3f(fromColor).div(255f),
			new Vector3f(toColor).div(255f),
			scale
		);
	}

	public static final MapCodec<RingExplosionParticleOptions> CODEC =
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			ExtraCodecs.VECTOR3F.fieldOf("from_color").forGetter(RingExplosionParticleOptions::fromColor),
			ExtraCodecs.VECTOR3F.fieldOf("to_color").forGetter(RingExplosionParticleOptions::toColor),
			Codec.FLOAT.fieldOf("scale").forGetter(RingExplosionParticleOptions::scale)
		).apply(instance, RingExplosionParticleOptions::new));

	public static final Deserializer<RingExplosionParticleOptions> DESERIALIZER =
		new Deserializer<>() {

			@Override
			public RingExplosionParticleOptions fromCommand(ParticleType<RingExplosionParticleOptions> type, StringReader reader) {
				return new RingExplosionParticleOptions(
					new Vector3f(1, 1, 1),
					new Vector3f(1, 1, 1),
					1F
				);
			}

			@Override
			public RingExplosionParticleOptions fromNetwork(ParticleType<RingExplosionParticleOptions> type, FriendlyByteBuf buf) {
				return new RingExplosionParticleOptions(
					buf.readVector3f(),
					buf.readVector3f(),
					buf.readFloat()
				);
			}
		};


	@Override
	public void writeToNetwork(FriendlyByteBuf buf) {
		buf.writeVector3f(fromColor);
		buf.writeVector3f(toColor);
		buf.writeFloat(scale);
	}

	@Override
	public String writeToString() {
		return BuiltInRegistries.PARTICLE_TYPE.getKey(getType()).toString();
	}

	@Override
	public ParticleType<RingExplosionParticleOptions> getType() {
		return ESParticles.RING_EXPLOSION.get();
	}
}
