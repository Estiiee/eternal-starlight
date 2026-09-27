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

public record RingParticleOptions(Vector3f fromColor, Vector3f toColor, float scale, float lifeScale, boolean gathering) implements ParticleOptions {

	public static RingParticleOptions fromIntColor(Vector3f fromColor, Vector3f toColor, float scale, float lifeScale, boolean gathering) {
		return new RingParticleOptions(
			new Vector3f(fromColor).div(255f),
			new Vector3f(toColor).div(255f),
			scale,
			lifeScale,
			gathering
		);
	}

	public static final MapCodec<RingParticleOptions> CODEC =
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			ExtraCodecs.VECTOR3F.fieldOf("from_color").forGetter(RingParticleOptions::fromColor),
			ExtraCodecs.VECTOR3F.fieldOf("to_color").forGetter(RingParticleOptions::toColor),
			Codec.FLOAT.fieldOf("scale").forGetter(RingParticleOptions::scale),
			Codec.FLOAT.fieldOf("life_scale").forGetter(RingParticleOptions::lifeScale),
			Codec.BOOL.fieldOf("gathering").forGetter(RingParticleOptions::gathering)
		).apply(instance, RingParticleOptions::new));

	public static final Deserializer<RingParticleOptions> DESERIALIZER =
		new Deserializer<>() {

			@Override
			public RingParticleOptions fromCommand(ParticleType<RingParticleOptions> type, StringReader reader) {
				return new RingParticleOptions(
					new Vector3f(1, 1, 1),
					new Vector3f(1, 1, 1),
					1F,
					1F,
					false
				);
			}

			@Override
			public RingParticleOptions fromNetwork(ParticleType<RingParticleOptions> type, FriendlyByteBuf buf) {
				return new RingParticleOptions(
					buf.readVector3f(),
					buf.readVector3f(),
					buf.readFloat(),
					buf.readFloat(),
					buf.readBoolean()
				);
			}
		};


	@Override
	public void writeToNetwork(FriendlyByteBuf buf) {
		buf.writeVector3f(fromColor);
		buf.writeVector3f(toColor);
		buf.writeFloat(scale);
		buf.writeFloat(lifeScale);
		buf.writeBoolean(gathering);
	}

	@Override
	public String writeToString() {
		return BuiltInRegistries.PARTICLE_TYPE.getKey(getType()).toString();
	}

	@Override
	public ParticleType<RingParticleOptions> getType() {
		return ESParticles.RING.get();
	}
}
