package cn.leolezury.eternalstarlight.common.particle;

import cn.leolezury.eternalstarlight.common.registry.ESParticles;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;

public record SurroundingTrailParticleOptions(
	Vector3f fromColor,
	Vector3f toColor,
	float radius,
	float rotSpeed,
	float alpha,
	int lifetime,
	int owner
) implements ParticleOptions
{

	public static SurroundingTrailParticleOptions fromIntColor(
		Vector3f fromColor,
		Vector3f toColor,
		float radius,
		float rotSpeed,
		float alpha,
		int lifetime,
		int owner
	) {
		return new SurroundingTrailParticleOptions(
			new Vector3f(fromColor).div(255f),
			new Vector3f(toColor).div(255f),
			radius,
			rotSpeed,
			alpha,
			lifetime,
			owner
		);
	}

	public static final MapCodec<SurroundingTrailParticleOptions> CODEC =
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			ExtraCodecs.VECTOR3F.fieldOf("from_color")
				.forGetter(SurroundingTrailParticleOptions::fromColor),
			ExtraCodecs.VECTOR3F.fieldOf("to_color")
				.forGetter(SurroundingTrailParticleOptions::toColor),
			Codec.FLOAT.fieldOf("radius")
				.forGetter(SurroundingTrailParticleOptions::radius),
			Codec.FLOAT.fieldOf("rot_speed")
				.forGetter(SurroundingTrailParticleOptions::rotSpeed),
			Codec.FLOAT.fieldOf("alpha")
				.forGetter(SurroundingTrailParticleOptions::alpha),
			Codec.INT.fieldOf("lifetime")
				.forGetter(SurroundingTrailParticleOptions::lifetime),
			Codec.INT.fieldOf("owner")
				.forGetter(SurroundingTrailParticleOptions::owner)
		).apply(instance, SurroundingTrailParticleOptions::new));

	public static final ParticleOptions.Deserializer<SurroundingTrailParticleOptions> DESERIALIZER =
		new ParticleOptions.Deserializer<>() {
			@Override
			public SurroundingTrailParticleOptions fromCommand(
				ParticleType<SurroundingTrailParticleOptions> type,
				StringReader reader
			) throws CommandSyntaxException
			{
				reader.expect(' ');

				Vector3f fromColor = readVector3f(reader);
				reader.expect(' ');

				Vector3f toColor = readVector3f(reader);
				reader.expect(' ');

				float radius = reader.readFloat();
				reader.expect(' ');

				float rotSpeed = reader.readFloat();
				reader.expect(' ');

				float alpha = reader.readFloat();
				reader.expect(' ');

				int lifetime = reader.readInt();
				reader.expect(' ');

				int owner = reader.readInt();

				return new SurroundingTrailParticleOptions(
					fromColor,
					toColor,
					radius,
					rotSpeed,
					alpha,
					lifetime,
					owner
				);
			}

			@Override
			public SurroundingTrailParticleOptions fromNetwork(
				ParticleType<SurroundingTrailParticleOptions> type,
				FriendlyByteBuf buf
			) {
				return SurroundingTrailParticleOptions.readFromNetwork(buf);
			}

			private static Vector3f readVector3f(StringReader reader) throws CommandSyntaxException {
				float x = reader.readFloat();
				reader.expect(' ');
				float y = reader.readFloat();
				reader.expect(' ');
				float z = reader.readFloat();

				return new Vector3f(x, y, z);
			}
		};

	public static SurroundingTrailParticleOptions readFromNetwork(FriendlyByteBuf buf) {
		Vector3f fromColor = new Vector3f(
			buf.readFloat(),
			buf.readFloat(),
			buf.readFloat()
		);

		Vector3f toColor = new Vector3f(
			buf.readFloat(),
			buf.readFloat(),
			buf.readFloat()
		);

		float radius = buf.readFloat();
		float rotSpeed = buf.readFloat();
		float alpha = buf.readFloat();
		int lifetime = buf.readVarInt();
		int owner = buf.readVarInt();

		return new SurroundingTrailParticleOptions(
			fromColor,
			toColor,
			radius,
			rotSpeed,
			alpha,
			lifetime,
			owner
		);
	}

	public void writeToNetwork(FriendlyByteBuf buf) {
		buf.writeFloat(fromColor.x());
		buf.writeFloat(fromColor.y());
		buf.writeFloat(fromColor.z());

		buf.writeFloat(toColor.x());
		buf.writeFloat(toColor.y());
		buf.writeFloat(toColor.z());

		buf.writeFloat(radius);
		buf.writeFloat(rotSpeed);
		buf.writeFloat(alpha);
		buf.writeVarInt(lifetime);
		buf.writeVarInt(owner);
	}

	public static SurroundingTrailParticleOptions magic(Player player) {
		RandomSource random = player.getRandom();

		return fromIntColor(
			new Vector3f(182, 48, 112),
			new Vector3f(99, 224, 235),
			0.8f + (random.nextFloat() - 0.5f) * 0.2f,
			(random.nextBoolean() ? -1 : 1)
				* (float) (15f + (random.nextFloat() - 0.5) * 7f),
			0.9f + (random.nextFloat() - 0.5f) * 0.1f,
			(int) (75 + (random.nextFloat() - 0.5) * 10),
			player.getId()
		);
	}

	@Override
	public ParticleType<SurroundingTrailParticleOptions> getType() {
		return ESParticles.SURROUNDING_TRAIL.get();
	}

	@Override
	public String writeToString() {
		return BuiltInRegistries.PARTICLE_TYPE.getKey(getType()).toString();
	}
}