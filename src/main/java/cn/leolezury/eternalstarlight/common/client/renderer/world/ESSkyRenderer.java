package cn.leolezury.eternalstarlight.common.client.renderer.world;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.weather.ClientWeatherState;
import cn.leolezury.eternalstarlight.common.config.ESConfig;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ESSkyRenderer {
	private static final ResourceLocation DEAD_STAR_LOCATION = EternalStarlight.id("textures/environment/dead_star.png");
	private static final float TIME = 12500f;
	private static final float FIXED_TIME = (float) (Mth.frac(TIME / 24000.0 - 0.25) * 2.0 + 0.5 - Math.cos(Mth.frac(TIME / 24000.0 - 0.25) * Math.PI) / 2.0) / 3.0F;
	private static VertexBuffer starBuffer;

	private static final double SKY_TRANSITION_SECONDS = 10.0;

	private static Vec3 currentSkyColor;
	private static long lastSkyUpdate;

	public static boolean renderSky(ClientLevel level, PoseStack poseStack, Matrix4f matrix, float partialTicks, Camera camera, Runnable setupFog) {
		Minecraft minecraft = Minecraft.getInstance();
		LevelRenderer levelRenderer = minecraft.levelRenderer;
		setupFog.run();

		if (starBuffer == null) {
			createStars();
		}

		FogType fogType = camera.getFluidInCamera();
		if (fogType != FogType.POWDER_SNOW && fogType != FogType.LAVA && !levelRenderer.doesMobEffectBlockSky(camera)) {

			BlockPos pos = camera.getBlockPosition();
			Holder<Biome> biomeHolder = level.getBiome(pos);
			Biome biome = biomeHolder.value();

			ResourceLocation biomeId = level.registryAccess().registryOrThrow(Registries.BIOME).getKey(biome);

			boolean useCustomSkyColor = biomeId != null && ESConfig.customSkyColorBiomes.get().contains(biomeId.toString());

			Vec3 targetSkyColor = null;

			if (useCustomSkyColor) {
				int rgb = biome.getModifiedSpecialEffects().getSkyColor();

				targetSkyColor = new Vec3(
					((rgb >> 16) & 0xFF) / 255.0,
					((rgb >> 8) & 0xFF) / 255.0,
					(rgb & 0xFF) / 255.0
				);
			}

			Vec3 vanillaSkyColor = level.getSkyColor(camera.getPosition(), partialTicks);

			Vec3 desiredSkyColor =
				targetSkyColor != null
					? targetSkyColor
					: vanillaSkyColor;

			long now = System.nanoTime();

			if (currentSkyColor == null) {
				currentSkyColor = vanillaSkyColor;
				lastSkyUpdate = now;
			} else {
				double elapsed = (now - lastSkyUpdate) / 1_000_000_000.0;
				double factor = Math.min(1.0, elapsed / SKY_TRANSITION_SECONDS);

				currentSkyColor = currentSkyColor.lerp(desiredSkyColor, factor);
				lastSkyUpdate = now;
			}

			Vec3 vec3 = currentSkyColor;
			float g = (float) vec3.x;
			float h = (float) vec3.y;
			float i = (float) vec3.z;
			FogRenderer.levelFogColor();
			Tesselator tesselator = Tesselator.getInstance();
			RenderSystem.depthMask(false);
			RenderSystem.setShaderColor(g, h, i, 1.0F);
			ShaderInstance shaderInstance = RenderSystem.getShader();
			levelRenderer.skyBuffer.bind();
			levelRenderer.skyBuffer.drawWithShader(poseStack.last().pose(), matrix, shaderInstance);
			VertexBuffer.unbind();
			RenderSystem.enableBlend();
			float[] fs = level.effects().getSunriseColor(FIXED_TIME, partialTicks);
			float j;
			float l;
			float p;
			float q;
			float r;
			if (fs != null) {
				RenderSystem.setShader(GameRenderer::getPositionColorShader);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				poseStack.pushPose();
				poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0F));
				j = Mth.sin(level.getSunAngle(partialTicks)) < 0.0F ? 180.0F : 0.0F;
				poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(j));
				poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(90.0F));
				float k = fs[0];
				l = fs[1];
				float m = fs[2];
				Matrix4f matrix4f3 = poseStack.last().pose();

				BufferBuilder bufferBuilder = tesselator.getBuilder();
				bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
				bufferBuilder
					.vertex(matrix4f3, 0.0F, 100.0F, 0.0F)
					.color(k, l, m, fs[3])
					.endVertex();

				for (int o = 0; o <= 16; ++o) {
					p = (float) o * 6.2831855F / 16.0F;
					q = Mth.sin(p);
					r = Mth.cos(p);
					bufferBuilder
						.vertex(matrix4f3, q * 120.0F, r * 120.0F, -r * 40.0F * fs[3])
						.color(fs[0], fs[1], fs[2], 0.0F)
						.endVertex();
				}

				BufferUploader.drawWithShader(bufferBuilder.end());
				poseStack.popPose();
			}

			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			poseStack.pushPose();

			float rainLevel = level.getRainLevel(partialTicks);
			if (ClientWeatherState.weather != null) {
				rainLevel = ClientWeatherState.weather.modifyRainLevel(rainLevel);
			} else {
				ClientWeatherState.levelTarget = rainLevel;
				rainLevel = ClientWeatherState.getRainLevel(partialTicks);
			}

			j = 1.0F - rainLevel;
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, j);
			poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90.0F));
			poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(FIXED_TIME * 360.0F));
			Matrix4f matrix4f4 = poseStack.last().pose();
			l = 60.0F;
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, DEAD_STAR_LOCATION);

			BufferBuilder bufferBuilder2 = tesselator.getBuilder();
			bufferBuilder2.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			bufferBuilder2
				.vertex(matrix4f4, -l, 100.0F, -l).uv(0.0F, 0.0F).endVertex();
			bufferBuilder2
				.vertex(matrix4f4,  l, 100.0F, -l).uv(1.0F, 0.0F).endVertex();
			bufferBuilder2
				.vertex(matrix4f4,  l, 100.0F,  l).uv(1.0F, 1.0F).endVertex();
			bufferBuilder2
				.vertex(matrix4f4, -l, 100.0F,  l).uv(0.0F, 1.0F).endVertex();
			BufferUploader.drawWithShader(bufferBuilder2.end());

			float v = 1.0f/*level.getStarBrightness(partialTicks) * j*/;
			RenderSystem.setShaderColor(v, v, v, v);
			FogRenderer.setupNoFog();
			starBuffer.bind();
			starBuffer.drawWithShader(poseStack.last().pose(), matrix, GameRenderer.getPositionShader());
			VertexBuffer.unbind();
			setupFog.run();

			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.disableBlend();
			RenderSystem.defaultBlendFunc();
			poseStack.popPose();
			RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);

			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.depthMask(true);
		}

		return true;
	}

	private static void createStars() {
		if (starBuffer != null) {
			starBuffer.close();
		}

		starBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
		starBuffer.bind();
		starBuffer.upload(drawStars(Tesselator.getInstance()));
		VertexBuffer.unbind();
	}

	private static BufferBuilder.RenderedBuffer drawStars(Tesselator tesselator) {
		RandomSource randomSource = RandomSource.create(10842L);
		float f = 100.0F;

		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

		for (int j = 0; j < 3000; ++j) {
			float g = randomSource.nextFloat() * 2.0F - 1.0F;
			float h = randomSource.nextFloat() * 2.0F - 1.0F;
			float k = randomSource.nextFloat() * 2.0F - 1.0F;
			float l = 0.15F + randomSource.nextFloat() * 0.1F;
			float m = (float) Mth.lengthSquared(g, h, k);

			if (!(m <= 0.010000001F) && !(m >= 1.0F)) {
				Vector3f vector3f = new Vector3f(g, h, k).normalize(f);
				float n = (float)(randomSource.nextDouble() * Math.PI * 2.0);
				Quaternionf quaternionf = new Quaternionf()
					.rotateTo(new Vector3f(0.0F, 0.0F, -1.0F), vector3f)
					.rotateZ(n);

				Vector3f v1 = new Vector3f(l, -l, 0.0F).rotate(quaternionf).add(vector3f);
				Vector3f v2 = new Vector3f(l,  l, 0.0F).rotate(quaternionf).add(vector3f);
				Vector3f v3 = new Vector3f(-l, l, 0.0F).rotate(quaternionf).add(vector3f);
				Vector3f v4 = new Vector3f(-l,-l, 0.0F).rotate(quaternionf).add(vector3f);

				bufferBuilder.vertex(v1.x, v1.y, v1.z).endVertex();
				bufferBuilder.vertex(v2.x, v2.y, v2.z).endVertex();
				bufferBuilder.vertex(v3.x, v3.y, v3.z).endVertex();
				bufferBuilder.vertex(v4.x, v4.y, v4.z).endVertex();
			}
		}

		return bufferBuilder.end();
	}
}
