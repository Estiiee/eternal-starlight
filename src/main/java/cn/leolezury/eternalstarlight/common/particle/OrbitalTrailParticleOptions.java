package cn.leolezury.eternalstarlight.common.particle;

import cn.leolezury.eternalstarlight.common.util.SmoothSegmentedValue;
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

public record OrbitalTrailParticleOptions(ParticleType<OrbitalTrailParticleOptions> type, Vector3f axis, SmoothSegmentedValue radius, SmoothSegmentedValue speed, float width, SmoothSegmentedValue length, Vector3f color, int lifetime) implements ParticleOptions {

	public static MapCodec<OrbitalTrailParticleOptions> codec(final ParticleType<OrbitalTrailParticleOptions> type) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
			ExtraCodecs.VECTOR3F.fieldOf("axis").forGetter(OrbitalTrailParticleOptions::axis),
			SmoothSegmentedValue.CODEC.fieldOf("radius").forGetter(OrbitalTrailParticleOptions::radius),
			SmoothSegmentedValue.CODEC.fieldOf("speed").forGetter(OrbitalTrailParticleOptions::speed),
			Codec.FLOAT.fieldOf("width").forGetter(OrbitalTrailParticleOptions::width),
			SmoothSegmentedValue.CODEC.fieldOf("length").forGetter(OrbitalTrailParticleOptions::length),
			ExtraCodecs.VECTOR3F.fieldOf("color").forGetter(OrbitalTrailParticleOptions::color),
			Codec.INT.fieldOf("lifetime").forGetter(OrbitalTrailParticleOptions::lifetime)
		).apply(instance, (axis, radius, speed, width, length, color, lifetime) ->
			new OrbitalTrailParticleOptions(type, axis, radius, speed, width, length, color, lifetime)));
	}

	public static final Deserializer<OrbitalTrailParticleOptions> DESERIALIZER =
		new Deserializer<>() {

			@Override
			public OrbitalTrailParticleOptions fromCommand(ParticleType<OrbitalTrailParticleOptions> type, StringReader reader) {
				return new OrbitalTrailParticleOptions(
					type,
					new Vector3f(0, 1, 0),
					SmoothSegmentedValue.constant(0f),
					SmoothSegmentedValue.constant(0f),
					1f,
					SmoothSegmentedValue.constant(0f),
					new Vector3f(1, 1, 1),
					1
				);
			}

			@Override
			public OrbitalTrailParticleOptions fromNetwork(ParticleType<OrbitalTrailParticleOptions> type, FriendlyByteBuf buf) {
				return new OrbitalTrailParticleOptions(
					type,
					buf.readVector3f(),
					SmoothSegmentedValue.readFromNetwork(buf),
					SmoothSegmentedValue.readFromNetwork(buf),
					buf.readFloat(),
					SmoothSegmentedValue.readFromNetwork(buf),
					buf.readVector3f(),
					buf.readInt()
				);
			}
		};

	@Override
	public void writeToNetwork(FriendlyByteBuf buf) {
		buf.writeVector3f(axis);
		radius.writeToNetwork(buf);
		speed.writeToNetwork(buf);
		buf.writeFloat(width);
		length.writeToNetwork(buf);
		buf.writeVector3f(color);
		buf.writeInt(lifetime);
	}

	@Override
	public String writeToString() {
		return BuiltInRegistries.PARTICLE_TYPE.getKey(getType()).toString();
	}

	@Override
	public ParticleType<OrbitalTrailParticleOptions> getType() {
		return type;
	}
}