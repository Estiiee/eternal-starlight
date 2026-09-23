package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.entity.attack.ray.RayAttack;
import cn.leolezury.eternalstarlight.common.entity.interfaces.RayAttackUser;
import cn.leolezury.eternalstarlight.common.util.ESMathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public abstract class LaserBeamRenderer<T extends RayAttack> extends EntityRenderer<T> {
	public LaserBeamRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	public float getTextureWidth() {
		return 8;
	}

	public float getEndLength() {
		return 0.5f;
	}

	public float getBeamWidth() {
		return 1;
	}

	@Override
	public void render(T laserBeam, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource bufferSource, int packedLight) {
		if (laserBeam.tickCount < 5) return;

		double entityX = Mth.lerp(partialTicks, laserBeam.xo, laserBeam.getX());
		double entityY = Mth.lerp(partialTicks, laserBeam.yo, laserBeam.getY());
		double entityZ = Mth.lerp(partialTicks, laserBeam.zo, laserBeam.getZ());

		Vec3 start = new Vec3(entityX, entityY, entityZ);

		stack.pushPose();

		if (laserBeam.getCaster().isPresent()) {
			Entity caster = laserBeam.getCaster().get();
			double posX = Mth.lerp(partialTicks, caster.xo, caster.getX());
			double posY = Mth.lerp(partialTicks, caster.yo, caster.getY());
			double posZ = Mth.lerp(partialTicks, caster.zo, caster.getZ());
			Vec3 pos = laserBeam.getPositionForCaster(caster, new Vec3(posX, posY, posZ));
			posX = pos.x;
			posY = pos.y;
			posZ = pos.z;
			stack.translate(posX - entityX, posY - entityY, posZ - entityZ);
			start = pos;
		}

		if (Minecraft.getInstance().options.getCameraType().isFirstPerson() && laserBeam.getCaster().isPresent() && laserBeam.getCaster().get() == Minecraft.getInstance().cameraEntity) {
			Vec3 offset = ESMathUtil.rotationToPosition(0.5f, -Minecraft.getInstance().cameraEntity.getXRot() - 90, Minecraft.getInstance().cameraEntity.getYHeadRot() + 90);
			stack.translate(offset.x, offset.y, offset.z);
			start = start.add(offset);
		}

		float yaw = Mth.lerp(partialTicks, laserBeam.prevYaw, laserBeam.renderYaw);
		float pitch = Mth.lerp(partialTicks, laserBeam.prevPitch, laserBeam.renderPitch);

		if (laserBeam.getCaster().isPresent()
			&& ((laserBeam.getCaster().get() instanceof RayAttackUser user && user.isRayFollowingHeadRotation())
			|| !(laserBeam.getCaster().get() instanceof RayAttackUser))
			&& laserBeam.getCaster().get() instanceof LivingEntity living) {
			yaw = (living.getViewYRot(partialTicks) + 90);
			pitch = -living.getViewXRot(partialTicks);
		}

		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		Vec3 sight = camera.getPosition().subtract(start);
		Vec3 diff = ESMathUtil.rotationToPosition(laserBeam.getLength(), pitch, yaw);
		Vec3 sideOffset = diff.cross(sight).normalize().scale((getBeamWidth() * 0.2 * Math.sin(laserBeam.tickCount + partialTicks) + getBeamWidth() * 0.8) / 2);
		PoseStack.Pose pose = stack.last();
		VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(laserBeam)));

		Vec3 bodyEndDiff = diff.normalize().scale(diff.length() - getEndLength());

		Matrix4f poseMat = pose.pose();
		Matrix3f normalMat = pose.normal();

		float r = 1f, g = 1f, b = 1f, a = 1f;

		consumer.vertex(poseMat, (float)sideOffset.x, (float)sideOffset.y, (float)sideOffset.z)
			.color(r, g, b, a)
			.uv(0f, 0f)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(LightTexture.FULL_BRIGHT)
			.normal(normalMat, 0f, 1f, 0f)
			.endVertex();

		consumer.vertex(poseMat, (float)(sideOffset.scale(-1).x), (float)(sideOffset.scale(-1).y), (float)(sideOffset.scale(-1).z))
			.color(r, g, b, a)
			.uv(0f, 1f)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(LightTexture.FULL_BRIGHT)
			.normal(normalMat, 0f, 1f, 0f)
			.endVertex();

		consumer.vertex(poseMat, (float)(bodyEndDiff.add(sideOffset.scale(-1)).x),
				(float)(bodyEndDiff.add(sideOffset.scale(-1)).y),
				(float)(bodyEndDiff.add(sideOffset.scale(-1)).z))
			.color(r, g, b, a)
			.uv(0f, 1f)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(LightTexture.FULL_BRIGHT)
			.normal(normalMat, 0f, 1f, 0f)
			.endVertex();

		consumer.vertex(poseMat, (float)(bodyEndDiff.add(sideOffset).x),
				(float)(bodyEndDiff.add(sideOffset).y),
				(float)(bodyEndDiff.add(sideOffset).z))
			.color(r, g, b, a)
			.uv(0f, 0f)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(LightTexture.FULL_BRIGHT)
			.normal(normalMat, 0f, 1f, 0f)
			.endVertex();

		consumer.vertex(poseMat, (float)(bodyEndDiff.add(sideOffset).x),
				(float)(bodyEndDiff.add(sideOffset).y),
				(float)(bodyEndDiff.add(sideOffset).z))
			.color(r, g, b, a)
			.uv(0f, 0f)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(LightTexture.FULL_BRIGHT)
			.normal(normalMat, 0f, 1f, 0f)
			.endVertex();

		consumer.vertex(poseMat, (float)(bodyEndDiff.add(sideOffset.scale(-1)).x),
				(float)(bodyEndDiff.add(sideOffset.scale(-1)).y),
				(float)(bodyEndDiff.add(sideOffset.scale(-1)).z))
			.color(r, g, b, a)
			.uv(0f, 1f)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(LightTexture.FULL_BRIGHT)
			.normal(normalMat, 0f, 1f, 0f)
			.endVertex();

		consumer.vertex(poseMat, (float)(diff.add(sideOffset.scale(-1)).x),
				(float)(diff.add(sideOffset.scale(-1)).y),
				(float)(diff.add(sideOffset.scale(-1)).z))
			.color(r, g, b, a)
			.uv(1f, 1f)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(LightTexture.FULL_BRIGHT)
			.normal(normalMat, 0f, 1f, 0f)
			.endVertex();

		consumer.vertex(poseMat, (float)(diff.add(sideOffset).x),
				(float)(diff.add(sideOffset).y),
				(float)(diff.add(sideOffset).z))
			.color(r, g, b, a)
			.uv(1f, 0f)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(LightTexture.FULL_BRIGHT)
			.normal(normalMat, 0f, 1f, 0f)
			.endVertex();

		stack.popPose();
	}
}
