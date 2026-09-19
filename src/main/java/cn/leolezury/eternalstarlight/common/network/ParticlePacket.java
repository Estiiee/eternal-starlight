package cn.leolezury.eternalstarlight.common.network;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.util.ESMiscUtil;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class ParticlePacket implements ESPacket {
	private final ParticleOptions particle;
	private final double x, y, z;
	private final double dx, dy, dz;
	private final boolean longDistance;

	public ParticlePacket(ParticleOptions particle,
						  double x, double y, double z,
						  double dx, double dy, double dz, boolean longDistance) {
		this.particle = particle;
		this.x = x; this.y = y; this.z = z;
		this.dx = dx; this.dy = dy; this.dz = dz;
		this.longDistance = longDistance;
	}

	public ParticlePacket(ParticleOptions particle, double x, double y, double z, double dx, double dy, double dz) {
		this(particle, x, y, z, dx, dy, dz, true);
	}

	public static ParticlePacket read(FriendlyByteBuf buf) {
		ParticleOptions particle = readParticle(buf);
		double x = buf.readDouble();
		double y = buf.readDouble();
		double z = buf.readDouble();
		double dx = buf.readDouble();
		double dy = buf.readDouble();
		double dz = buf.readDouble();
		boolean longDistance = buf.readBoolean();
		return new ParticlePacket(particle, x, y, z, dx, dy, dz, longDistance);
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		writeParticle(particle, buf);
		buf.writeDouble(x);
		buf.writeDouble(y);
		buf.writeDouble(z);
		buf.writeDouble(dx);
		buf.writeDouble(dy);
		buf.writeDouble(dz);
		buf.writeBoolean(longDistance);
	}

	private static ParticleOptions readParticle(FriendlyByteBuf buf) {
		int typeId = buf.readVarInt();
		ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.byId(typeId);

		if (type == null)
			throw new IllegalStateException("Unknown particle type id: " + typeId);

		ParticleOptions.Deserializer<?> deser = type.getDeserializer();

		if (deser == null)
			throw new IllegalStateException("Particle type has no deserializer: " + type);

		@SuppressWarnings("unchecked")
		ParticleOptions.Deserializer<ParticleOptions> casted =
			(ParticleOptions.Deserializer<ParticleOptions>) deser;

		return casted.fromNetwork((ParticleType<ParticleOptions>) type, buf);
	}

	private static void writeParticle(ParticleOptions particle, FriendlyByteBuf buf) {
		buf.writeVarInt(BuiltInRegistries.PARTICLE_TYPE.getId(particle.getType()));
		particle.writeToNetwork(buf);
	}

	@Override
	public void handle(Player player) {
		ESMiscUtil.runWhenOnClient(() ->
			() -> EternalStarlight.getClientHelper().handleParticlePacket(this)
		);
	}

	@Override
	public ResourceLocation id() {
		return EternalStarlight.id("particle");
	}

	public ParticleOptions particle() { return particle; }
	public double x() { return x; }
	public double y() { return y; }
	public double z() { return z; }
	public double dx() { return dx; }
	public double dy() { return dy; }
	public double dz() { return dz; }
	public boolean longDistance() { return longDistance; }
}
