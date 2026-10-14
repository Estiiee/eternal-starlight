package cn.leolezury.eternalstarlight.common.client.particle.effect;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SonarParticle extends SimpleAnimatedParticle {
	private final float rot;
	private final float pitch;

	protected SonarParticle(ClientLevel clientLevel, double x, double y, double z, double dx, double dy, double dz, SpriteSet spriteSet) {
		super(clientLevel, x, y, z, spriteSet, 0);
		this.friction = 1F;
		this.xd = dx;
		this.yd = dy;
		this.zd = dz;
		this.quadSize = this.random.nextFloat() * this.random.nextFloat() * 2.0F + 2.0F;
		this.lifetime = (int) (100f + this.random.nextFloat());
		this.rot = (float) Mth.atan2(-dx, -dz);
		this.pitch = (float) Mth.atan2(-dy, Math.sqrt(dx * dx + dz * dz));
		this.setSpriteFromAge(spriteSet);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.onGround || this.xd == 0.0 || this.zd == 0.0) {
			this.remove();
		}
		alpha = 1f - (float) age / lifetime;
	}

	@Override
	public float getQuadSize(float partialTicks) {
		return this.quadSize * Mth.clamp(((float) this.age + partialTicks) * 1.5F / (float) this.lifetime, 0.0F, 1.0F) * 0.75f;
	}

	@Override
	public void render(VertexConsumer vertexConsumer, Camera camera, float partialTicks) {
		// float f = Mth.sin(((float)this.age + partialTicks - 6.2831855F) * 0.05F) * 2.0F;
		Quaternionf quaternionf = new Quaternionf();
		quaternionf.rotateY(rot).rotateX(-pitch);
		this.renderRotatedQuad(vertexConsumer, camera, quaternionf, partialTicks);
		quaternionf = new Quaternionf();
		quaternionf.rotateY(-3.1415927F + rot).rotateX(pitch);
		this.renderRotatedQuad(vertexConsumer, camera, quaternionf, partialTicks);
	}

	//copypaste from RingExplosionParticle
	private void renderRotatedQuad(VertexConsumer consumer, Camera camera, Quaternionf rotation, float partialTicks) {
		Vec3 camPos = camera.getPosition();

		float x = (float) (Mth.lerp(partialTicks, xo, this.x) - camPos.x);
		float y = (float) (Mth.lerp(partialTicks, yo, this.y) - camPos.y);
		float z = (float) (Mth.lerp(partialTicks, zo, this.z) - camPos.z);

		float size = this.getQuadSize(partialTicks);

		Vector3f p0 = new Vector3f(-size, -size, 0);
		Vector3f p1 = new Vector3f(-size,  size, 0);
		Vector3f p2 = new Vector3f( size,  size, 0);
		Vector3f p3 = new Vector3f( size, -size, 0);

		p0.rotate(rotation);
		p1.rotate(rotation);
		p2.rotate(rotation);
		p3.rotate(rotation);

		p0.add(x, y, z);
		p1.add(x, y, z);
		p2.add(x, y, z);
		p3.add(x, y, z);

		float u0 = this.getU0();
		float u1 = this.getU1();
		float v0 = this.getV0();
		float v1 = this.getV1();

		float r = this.rCol;
		float g = this.gCol;
		float b = this.bCol;
		float a = this.alpha;

		Vector3f normal = new Vector3f(0, 0, 1);
		normal.rotate(rotation);

		consumer.vertex(p0.x, p0.y, p0.z).uv(u0, v1).color(r, g, b, a).uv2(LightTexture.FULL_BRIGHT).endVertex();
		consumer.vertex(p1.x, p1.y, p1.z).uv(u0, v0).color(r, g, b, a).uv2(LightTexture.FULL_BRIGHT).endVertex();
		consumer.vertex(p2.x, p2.y, p2.z).uv(u1, v0).color(r, g, b, a).uv2(LightTexture.FULL_BRIGHT).endVertex();
		consumer.vertex(p3.x, p3.y, p3.z).uv(u1, v1).color(r, g, b, a).uv2(LightTexture.FULL_BRIGHT).endVertex();
	}

	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Provider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
			return new SonarParticle(level, x, y, z, dx, dy, dz, this.sprites);
		}
	}
}
