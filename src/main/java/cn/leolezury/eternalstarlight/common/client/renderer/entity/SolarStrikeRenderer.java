package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.client.ESRenderType;
import cn.leolezury.eternalstarlight.common.client.handler.ESClientHandler;
import cn.leolezury.eternalstarlight.common.entity.attack.SolarStrikeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class SolarStrikeRenderer extends EntityRenderer<SolarStrikeEntity> {
	public SolarStrikeRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(SolarStrikeEntity entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource bufferSource, int packedLight) {
		float age = entity.getAnimationTicks(partialTicks);

		float width;
		float alpha;
		int chargeDuration = entity.getChargeDuration();
		if (age < chargeDuration) {
			float progress = age / chargeDuration;
			width = Mth.lerp(progress, 1.6F, 0.0F);
			alpha = Mth.lerp(progress, 0.0F, 1.0F);
		} else if (age < chargeDuration + SolarStrikeEntity.DAMAGE_TIME) {
			float damageProgress = Mth.clamp((age - chargeDuration) / (SolarStrikeEntity.DAMAGE_TIME / 2f), 0, 1);
			width = Mth.lerp(damageProgress, 0.0F, 0.3F);
			alpha = 1.0F;
		} else {
			float fadeProgress = (age - chargeDuration - SolarStrikeEntity.DAMAGE_TIME) / SolarStrikeEntity.FADE_TIME;
			width = Mth.lerp(fadeProgress, 0.3F, 0.4F);
			alpha = Mth.lerp(fadeProgress, 1.0F, 0.0F);
		}

		if (width <= 0.0F || alpha <= 0.0F) {
			return;
		}

		double entityX = Mth.lerp(partialTicks, entity.xo, entity.getX());
		double entityY = Mth.lerp(partialTicks, entity.yo, entity.getY());
		double entityZ = Mth.lerp(partialTicks, entity.zo, entity.getZ());
		Vec3 pos = new Vec3(entityX, entityY, entityZ);

		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		Vec3 camPos = camera.getPosition();

		VertexConsumer vertexConsumer = ESClientHandler.DELAYED_BUFFER_SOURCE.getBuffer(ESRenderType.DRAGON_RAYS_QUADS);
		PoseStack.Pose pose = stack.last();

		Vec3 end = entity.getBeamTarget();
		Vec3 offset = end.subtract(pos);
		Vec3 sight = camPos.subtract(pos).scale(-1);
		Vec3 sideOffset = offset.cross(sight).normalize().scale(width / 2);

		Vector3f v1 = sideOffset.toVector3f();
		vertexConsumer.vertex(pose.pose(), v1.x(), v1.y(), v1.z())
			.color(255 / 255f, 213 / 255f, 74 / 255f, alpha)
			.endVertex();

		Vector3f v2 = sideOffset.scale(-1).toVector3f();
		vertexConsumer.vertex(pose.pose(), v2.x(), v2.y(), v2.z())
			.color(255 / 255f, 213 / 255f, 74 / 255f, alpha)
			.endVertex();

		Vector3f v3 = offset.add(sideOffset.scale(-1)).toVector3f();
		vertexConsumer.vertex(pose.pose(), v3.x(), v3.y(), v3.z())
			.color(255 / 255f, 213 / 255f, 74 / 255f, 0)
			.endVertex();

		Vector3f v4 = offset.add(sideOffset).toVector3f();
		vertexConsumer.vertex(pose.pose(), v4.x(), v4.y(), v4.z())
			.color(255 / 255f, 213 / 255f, 74 / 255f, 0)
			.endVertex();

		end = pos.add(pos.subtract(entity.getBeamTarget()));
		offset = end.subtract(pos);
		sideOffset = offset.cross(sight).normalize().scale(width / 2);

		Vector3f v5 = sideOffset.toVector3f();
		vertexConsumer.vertex(pose.pose(), v5.x(), v5.y(), v5.z())
			.color(255 / 255f, 213 / 255f, 74 / 255f, alpha)
			.endVertex();

		Vector3f v6 = sideOffset.scale(-1).toVector3f();
		vertexConsumer.vertex(pose.pose(), v6.x(), v6.y(), v6.z())
			.color(255 / 255f, 213 / 255f, 74 / 255f, alpha)
			.endVertex();

		Vector3f v7 = offset.add(sideOffset.scale(-1)).toVector3f();
		vertexConsumer.vertex(pose.pose(), v7.x(), v7.y(), v7.z())
			.color(255 / 255f, 213 / 255f, 74 / 255f, 0)
			.endVertex();

		Vector3f v8 = offset.add(sideOffset).toVector3f();
		vertexConsumer.vertex(pose.pose(), v8.x(), v8.y(), v8.z())
			.color(255 / 255f, 213 / 255f, 74 / 255f, 0)
			.endVertex();

		super.render(entity, entityYaw, partialTicks, stack, bufferSource, packedLight);
	}

	@Override
	public ResourceLocation getTextureLocation(SolarStrikeEntity entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}