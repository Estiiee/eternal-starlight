package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.ESRenderType;
import cn.leolezury.eternalstarlight.common.client.model.entity.OrbModel;
import cn.leolezury.eternalstarlight.common.client.model.entity.SolarCreeperModel;
import cn.leolezury.eternalstarlight.common.entity.living.boss.creeper.*;
import cn.leolezury.eternalstarlight.common.util.ESMathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SolarCreeperRenderer<T extends SolarCreeper> extends MobRenderer<T, SolarCreeperModel<T>> {
	private static final ResourceLocation ENTITY_TEXTURE = EternalStarlight.id("textures/entity/solar_creeper/solar_creeper.png");
	private static final ResourceLocation SUN_TEXTURE = EternalStarlight.id("textures/entity/solar_creeper/sun.png");
	private static final ResourceLocation BLACK_HOLE_TEXTURE = EternalStarlight.id("textures/entity/solar_creeper/black_hole.png");
	private static final ResourceLocation LASER_JITTER_TEXTURE = EternalStarlight.id("textures/entity/solar_creeper/solar_ray_jitter.png");
	private static final ResourceLocation LASER_STATIC_TEXTURE = EternalStarlight.id("textures/entity/solar_creeper/solar_ray_static.png");

	private final OrbModel<Entity> sunModel;

	public SolarCreeperRenderer(EntityRendererProvider.Context context) {
		super(context, new SolarCreeperModel<>(context.bakeLayer(SolarCreeperModel.LAYER_LOCATION)), 0.5f);
		this.sunModel = new OrbModel<>(context.bakeLayer(OrbModel.LAYER_LOCATION));
	}

	@Override
	public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		if (entity.tickCount < 3) return;
		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		int state = entity.getBehaviorState();
		float animationTicks = entity.getAnimationTicks(partialTicks);
		float bodyScale = 1, sunScale = 0, blackHoleScale = 0, shineScale = 0, fullDuration = 0;
		int sunColor = -1;
		Vec3 pos = new Vec3(
			Mth.lerp(partialTicks, entity.xo, entity.getX()),
			Mth.lerp(partialTicks, entity.yo, entity.getY()),
			Mth.lerp(partialTicks, entity.zo, entity.getZ())
		);
		Vec3 sunAbovePos = entity.getSunAbovePos(partialTicks);

		if (entity.getBehaviorState() == SolarCreeperIntroPhase.ID) {
			this.shadowRadius = 0.0F;
		} else {
			this.shadowRadius = 0.5F;
		}

		if (state == SolarCreeperIntroPhase.ID) {
			bodyScale = SolarCreeperIntroPhase.BODY_SCALE.calculate(animationTicks / SolarCreeperIntroPhase.DURATION);
			sunScale = 2 * SolarCreeperIntroPhase.SUN_SCALE.calculate(animationTicks / SolarCreeperIntroPhase.DURATION);
			shineScale = SolarCreeperIntroPhase.SHINE_SCALE.calculate(animationTicks / SolarCreeperIntroPhase.DURATION);
			fullDuration = SolarCreeperIntroPhase.DURATION;
		}
		if (state == SolarCreeperSupernovaPhase.ID) {
			bodyScale = SolarCreeperSupernovaPhase.BODY_SCALE.calculate(animationTicks / SolarCreeperSupernovaPhase.DURATION);
			sunScale = 2 * SolarCreeperSupernovaPhase.SUN_SCALE.calculate(animationTicks / SolarCreeperSupernovaPhase.DURATION);
			shineScale = SolarCreeperSupernovaPhase.SHINE_SCALE.calculate(animationTicks / SolarCreeperSupernovaPhase.DURATION);
			fullDuration = SolarCreeperSupernovaPhase.DURATION;
		}
		if (state == SolarCreeperSolarRayPhase.ID) {
			sunScale = 2 * SolarCreeperSolarRayPhase.SUN_SCALE.calculate(animationTicks / SolarCreeperSolarRayPhase.DURATION);
			shineScale = SolarCreeperSolarRayPhase.SHINE_SCALE.calculate(animationTicks / SolarCreeperSolarRayPhase.DURATION);
			fullDuration = SolarCreeperSolarRayPhase.DURATION;
		}
		if (state == SolarCreeperBlackHolePhase.ID) {
			float progress = animationTicks / SolarCreeperBlackHolePhase.DURATION;
			float baseScale = 2 * SolarCreeperBlackHolePhase.SUN_SCALE.calculate(progress);
			float jitterFreq = SolarCreeperBlackHolePhase.JITTER_FREQ.calculate(progress);
			float jitter = 1 + 0.12f * Mth.sin(jitterFreq * animationTicks * 0.3f);
			sunScale = baseScale * jitter;
			float redness = SolarCreeperBlackHolePhase.SUN_REDNESS.calculate(progress);
			sunColor = FastColor.ARGB32.color(255, 255, (int) Mth.lerp(redness, 255, 80), (int) Mth.lerp(redness, 255, 80));
			blackHoleScale = SolarCreeperBlackHolePhase.BLACK_HOLE_SCALE.calculate(progress);
			shineScale = SolarCreeperBlackHolePhase.SHINE_SCALE.calculate(progress) * jitter;
			fullDuration = SolarCreeperBlackHolePhase.DURATION;
		}
		if (state == SolarCreeperGalaxyPhase.ID) {
			float progress = animationTicks / SolarCreeperGalaxyPhase.DURATION;
			float baseScale = SolarCreeperGalaxyPhase.BODY_SCALE.calculate(progress);
			float jitterFreq = SolarCreeperGalaxyPhase.JITTER_FREQ.calculate(progress);
			float jitter = 1 + 0.12f * Mth.sin(jitterFreq * animationTicks * 0.3f);
			bodyScale = baseScale * jitter;
			fullDuration = SolarCreeperGalaxyPhase.DURATION;
		}

		if (bodyScale > 0) {
			poseStack.pushPose();
			poseStack.translate(0.0F, entity.getBbHeight() / 2, 0.0F);
			poseStack.scale(bodyScale, bodyScale, bodyScale);
			poseStack.translate(0.0F, -entity.getBbHeight() / 2, 0.0F);
			super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
			poseStack.popPose();
		}
		if (entity.getBehaviorTicks() < 3) return;
		if (sunScale > 0) {
			poseStack.pushPose();
			if (state == SolarCreeperSolarRayPhase.ID || state == SolarCreeperBlackHolePhase.ID) {
				poseStack.translate(sunAbovePos.x - pos.x, sunAbovePos.y - pos.y, sunAbovePos.z - pos.z);
			} else {
				poseStack.translate(0.0F, entity.getBbHeight() / 2, 0.0F);
			}
			poseStack.scale(sunScale, sunScale, sunScale);
			poseStack.scale(-1.0F, -1.0F, 1.0F);
			poseStack.translate(0.0F, -1.5F, 0.0F);
			RenderType renderType = this.sunModel.renderType(SUN_TEXTURE);
			VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
			this.sunModel.setupAnim(entity, 0, 0, getBob(entity, partialTicks), 0, 0);
			float r = FastColor.ARGB32.red(sunColor) / 255f;
			float g = FastColor.ARGB32.green(sunColor) / 255f;
			float b = FastColor.ARGB32.blue(sunColor) / 255f;
			float a = FastColor.ARGB32.alpha(sunColor) / 255f;
			this.sunModel.renderToBuffer(poseStack, vertexConsumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, r, g, b, a);
			poseStack.popPose();
		}
		if (blackHoleScale > 0) {
			poseStack.pushPose();
			poseStack.translate(sunAbovePos.x - pos.x, sunAbovePos.y - pos.y, sunAbovePos.z - pos.z);
			poseStack.scale(blackHoleScale, blackHoleScale, blackHoleScale);
			poseStack.scale(-1.0F, -1.0F, 1.0F);
			poseStack.translate(0.0F, -1.5F, 0.0F);
			RenderType renderType = this.sunModel.renderType(BLACK_HOLE_TEXTURE);
			VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
			this.sunModel.setupAnim(entity, 0, 0, getBob(entity, partialTicks), 0, 0);
			this.sunModel.renderToBuffer(poseStack, vertexConsumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			poseStack.popPose();
		}
		if (shineScale > 0) {
			poseStack.pushPose();
			if (state == SolarCreeperSolarRayPhase.ID || state == SolarCreeperBlackHolePhase.ID) {
				poseStack.translate(sunAbovePos.x - pos.x, sunAbovePos.y - pos.y, sunAbovePos.z - pos.z);
			} else {
				poseStack.translate(0.0F, entity.getBbHeight() / 2, 0.0F);
			}
			poseStack.scale(shineScale, shineScale, shineScale);
			if (state == SolarCreeperBlackHolePhase.ID) {
				poseStack.scale(entity.getBbHeight() * 4, entity.getBbHeight() * 4, entity.getBbHeight() * 4);
				PoseStack.Pose pose = poseStack.last();
				VertexConsumer vertexConsumer = buffer.getBuffer(ESRenderType.DRAGON_RAYS);
				Matrix4f poseMat = pose.pose();
				Matrix3f normalMat = pose.normal();

				Vec3 camPos = camera.getPosition();
				Vec3 sight = camPos.subtract(sunAbovePos);
				Vec3 sideOffset = SolarCreeperBlackHolePhase.BLACK_HOLE_RAY_NORMAL.cross(sight).normalize().scale(0.2);

				int coreColor = FastColor.ARGB32.color(255, 139, 38, 19);
				int edgeColor = FastColor.ARGB32.color(0, 229, 84, 6);

				vertexConsumer.vertex(poseMat, 0f, 0f, 0f)
					.color(coreColor)
					.uv(0f, 0f)
					.overlayCoords(OverlayTexture.NO_OVERLAY)
					.uv2(LightTexture.FULL_BRIGHT)
					.normal(normalMat, 0f, 1f, 0f)
					.endVertex();

				Vector3f v1 = SolarCreeperBlackHolePhase.BLACK_HOLE_RAY_NORMAL.add(sideOffset).toVector3f();
				vertexConsumer.vertex(poseMat, v1.x(), v1.y(), v1.z())
					.color(edgeColor)
					.uv(0f, 1f)
					.overlayCoords(OverlayTexture.NO_OVERLAY)
					.uv2(LightTexture.FULL_BRIGHT)
					.normal(normalMat, 0f, 1f, 0f)
					.endVertex();

				Vector3f v2 = SolarCreeperBlackHolePhase.BLACK_HOLE_RAY_NORMAL.subtract(sideOffset).toVector3f();
				vertexConsumer.vertex(poseMat, v2.x(), v2.y(), v2.z())
					.color(edgeColor)
					.uv(1f, 1f)
					.overlayCoords(OverlayTexture.NO_OVERLAY)
					.uv2(LightTexture.FULL_BRIGHT)
					.normal(normalMat, 0f, 1f, 0f)
					.endVertex();

				vertexConsumer.vertex(poseMat, 0f, 0f, 0f)
					.color(coreColor)
					.uv(0f, 0f)
					.overlayCoords(OverlayTexture.NO_OVERLAY)
					.uv2(LightTexture.FULL_BRIGHT)
					.normal(normalMat, 0f, 1f, 0f)
					.endVertex();

				Vector3f v3 = SolarCreeperBlackHolePhase.BLACK_HOLE_RAY_NORMAL.scale(-1).subtract(sideOffset).toVector3f();
				vertexConsumer.vertex(poseMat, v3.x(), v3.y(), v3.z())
					.color(edgeColor)
					.uv(1f, 1f)
					.overlayCoords(OverlayTexture.NO_OVERLAY)
					.uv2(LightTexture.FULL_BRIGHT)
					.normal(normalMat, 0f, 1f, 0f)
					.endVertex();

				Vector3f v4 = SolarCreeperBlackHolePhase.BLACK_HOLE_RAY_NORMAL.scale(-1).add(sideOffset).toVector3f();
				vertexConsumer.vertex(poseMat, v4.x(), v4.y(), v4.z())
					.color(edgeColor)
					.uv(0f, 1f)
					.overlayCoords(OverlayTexture.NO_OVERLAY)
					.uv2(LightTexture.FULL_BRIGHT)
					.normal(normalMat, 0f, 1f, 0f)
					.endVertex();
			} else {
				poseStack.mulPose(new Quaternionf(this.entityRenderDispatcher.cameraOrientation()).rotateY(Mth.PI));
				PoseStack.Pose pose = poseStack.last();
				VertexConsumer vertexConsumer = buffer.getBuffer(ESRenderType.DRAGON_RAYS);
				Matrix4f poseMat = pose.pose();
				Matrix3f normalMat = pose.normal();

				int rays = 5;
				int coreColor = FastColor.ARGB32.color(255, 255, 213, 74);
				int edgeColor = FastColor.ARGB32.color(0, 255, 213, 74);

				for (int i = 0; i < rays; i++) {
					vertexConsumer.vertex(poseMat, 0f, 0f, 0f)
						.color(coreColor)
						.uv(0f, 0f)
						.overlayCoords(OverlayTexture.NO_OVERLAY)
						.uv2(LightTexture.FULL_BRIGHT)
						.normal(normalMat, 0f, 1f, 0f)
						.endVertex();

					float angle = i * Mth.TWO_PI / rays + (animationTicks / fullDuration) * Mth.PI * 1.5f;
					float x1 = Mth.sin(angle) * entity.getBbHeight() * 3;
					float y1 = Mth.cos(angle) * entity.getBbHeight() * 3;

					vertexConsumer.vertex(poseMat, x1, y1, 0f)
						.color(edgeColor)
						.uv(0f, 1f)
						.overlayCoords(OverlayTexture.NO_OVERLAY)
						.uv2(LightTexture.FULL_BRIGHT)
						.normal(normalMat, 0f, 1f, 0f)
						.endVertex();

					float largerAngle = angle + Mth.TWO_PI / 12;
					float x2 = Mth.sin(largerAngle) * entity.getBbHeight() * 3;
					float y2 = Mth.cos(largerAngle) * entity.getBbHeight() * 3;

					vertexConsumer.vertex(poseMat, x2, y2, 0f)
						.color(edgeColor)
						.uv(1f, 1f)
						.overlayCoords(OverlayTexture.NO_OVERLAY)
						.uv2(LightTexture.FULL_BRIGHT)
						.normal(normalMat, 0f, 1f, 0f)
						.endVertex();
				}
			}
			poseStack.popPose();
		}
		if (state == SolarCreeperSolarRayPhase.ID) {
			Vec3 sight = camera.getPosition().subtract(sunAbovePos);
			Vector3f normalVec = entity.getRenderSolarRayNormal(partialTicks);
			float angle = entity.getRenderSolarRayAngle(partialTicks);
			Vec3 n = new Vec3(normalVec);
			if (n.lengthSqr() < 0.01) n = new Vec3(0, 1, 0);
			n = n.normalize();
			Vec3 zOffset = sunAbovePos.subtract(camera.getPosition()).normalize().scale(0.01);
			poseStack.pushPose();
			poseStack.translate(sunAbovePos.x - pos.x, sunAbovePos.y - pos.y, sunAbovePos.z - pos.z);
			PoseStack.Pose pose = poseStack.last();

			int laserColor = FastColor.ARGB32.color(255, 255, 213, 74);

			for (int i = 0; i < 6; i++) {
				if (entity.getRenderSolarRayWidth(i, partialTicks) > 0) {
					float length = entity.getRenderSolarRayLength(i, partialTicks);
					Vec3 diff = ESMathUtil.rotateAroundAxis(n, i * 60 + angle, length);
					Vec3 bodyEndDiff = diff.normalize().scale(Math.max(diff.length() - 0.3f, 0));
					float jitterWidth = 0.8f;
					jitterWidth = jitterWidth * 0.2f * (float) Math.sin((entity.tickCount + partialTicks) * 2.1f) + jitterWidth * 0.8f;
					jitterWidth *= entity.getRenderSolarRayWidth(i, partialTicks);
					Vec3 jitterOffset = diff.cross(sight).normalize().scale(jitterWidth / 2);
					float staticWidth = 0.8f;
					staticWidth *= entity.getRenderSolarRayWidth(i, partialTicks);
					Vec3 staticOffset = diff.cross(sight).normalize().scale(staticWidth / 2);
					VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(LASER_JITTER_TEXTURE));

					vertex(consumer, pose, jitterOffset.toVector3f(), 0, 0, laserColor);
					vertex(consumer, pose, jitterOffset.scale(-1).toVector3f(), 0, 1, laserColor);
					vertex(consumer, pose, bodyEndDiff.add(jitterOffset.scale(-1)).toVector3f(), 0, 1);
					vertex(consumer, pose, bodyEndDiff.add(jitterOffset).toVector3f(), 0, 0);

					vertex(consumer, pose, bodyEndDiff.add(jitterOffset).toVector3f(), 0, 0);
					vertex(consumer, pose, bodyEndDiff.add(jitterOffset.scale(-1)).toVector3f(), 0, 1);
					vertex(consumer, pose, diff.add(jitterOffset.scale(-1)).toVector3f(), 1, 1);
					vertex(consumer, pose, diff.add(jitterOffset).toVector3f(), 1, 0);

					consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(LASER_STATIC_TEXTURE));

					vertex(consumer, pose, staticOffset.add(zOffset).toVector3f(), 0, 0, laserColor);
					vertex(consumer, pose, staticOffset.scale(-1).add(zOffset).toVector3f(), 0, 1, laserColor);
					vertex(consumer, pose, bodyEndDiff.add(staticOffset.scale(-1)).add(zOffset).toVector3f(), 0, 1);
					vertex(consumer, pose, bodyEndDiff.add(staticOffset).add(zOffset).toVector3f(), 0, 0);

					vertex(consumer, pose, bodyEndDiff.add(staticOffset).add(zOffset).toVector3f(), 0, 0);
					vertex(consumer, pose, bodyEndDiff.add(staticOffset.scale(-1)).add(zOffset).toVector3f(), 0, 1);
					vertex(consumer, pose, diff.add(staticOffset.scale(-1)).add(zOffset).toVector3f(), 1, 1);
					vertex(consumer, pose, diff.add(staticOffset).add(zOffset).toVector3f(), 1, 0);
				}
			}
			poseStack.popPose();
		}
	}

	private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, Vector3f pos, float u, float v) {
		vertex(consumer, pose, pos, u, v, -1);
	}

	private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, Vector3f pos, float u, float v, int color) {
		consumer.vertex(pose.pose(), pos.x(), pos.y(), pos.z())
			.color(color)
			.uv(u, v)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(LightTexture.FULL_BRIGHT)
			.normal(pose.normal(), 0, 1, 0)
			.endVertex();
	}

	@Override
	protected float getFlipDegrees(T entity) {
		return 0;
	}

	@Override
	protected float getWhiteOverlayProgress(T entity, float partialTicks) {
		int state = entity.getBehaviorState();
		float animationTicks = entity.getAnimationTicks(partialTicks);
		float shineScale = 0;
		if (entity.getBehaviorTicks() >= 3) {
			if (state == SolarCreeperIntroPhase.ID) {
				shineScale = SolarCreeperIntroPhase.SHINE_SCALE.calculate(animationTicks / SolarCreeperIntroPhase.DURATION);
			}
			if (state == SolarCreeperSupernovaPhase.ID) {
				shineScale = SolarCreeperSupernovaPhase.SHINE_SCALE.calculate(animationTicks / SolarCreeperSupernovaPhase.DURATION);
			}
			if (state == SolarCreeperGalaxyPhase.ID) {
				shineScale = SolarCreeperGalaxyPhase.SHINE_SCALE.calculate(animationTicks / SolarCreeperGalaxyPhase.DURATION);
			}
		}
		return Mth.clamp(shineScale, 0, 1);
	}

	/*
	@Override
	protected float getShadowRadius(T mob) {
		return mob.getBehaviorState() == SolarCreeperIntroPhase.ID ? 0 : super.getShadowRadius(mob);
	}
	 */

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return ENTITY_TEXTURE;
	}
}
