package cn.leolezury.eternalstarlight.common.client;

import cn.leolezury.eternalstarlight.common.client.renderer.world.ESSkyRenderer;
import cn.leolezury.eternalstarlight.common.config.ESConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class ESDimensionSpecialEffects extends DimensionSpecialEffects {
	public ESDimensionSpecialEffects(float cloudHeight, boolean placebo, SkyType fogType, boolean brightenLightMap, boolean entityLightingBottomsLit) {
		super(cloudHeight, placebo, fogType, brightenLightMap, entityLightingBottomsLit);
	}

	private static Vec3 lastFogColor;
	private static long lastFogUpdate;

	@Override
	public Vec3 getBrightnessDependentFogColor(Vec3 biomeFogColor, float daylight) {
		long now = System.nanoTime();

		if (lastFogColor == null) {
			lastFogColor = biomeFogColor;
			lastFogUpdate = now;
		} else {
			double elapsed = (now - lastFogUpdate) / 1_000_000_000.0;
			double factor = Math.min(1.0, elapsed / 10.0);

			lastFogColor = lastFogColor.lerp(biomeFogColor, factor);
			lastFogUpdate = now;
		}

		float daylightFactor = daylight * 0.94F + 0.06F;
		float blueFactor = daylight * 0.91F + 0.09F;

		return lastFogColor.multiply(daylightFactor, daylightFactor, blueFactor);
	}

	@Override
	public boolean isFoggyAt(int x, int y) {
		return false;
	}

	@Override
	public float[] getSunriseColor(float timeOfDay, float partialTick) {
		if (!ESConfig.renderSunriseColor.get()) return null;
		float[] sunriseColor = parseSunrise(ESConfig.sunriseColor.get());
		return sunriseColor != null ? sunriseColor : super.getSunriseColor(timeOfDay, partialTick);
	}

	public float[] parseSunrise(String s) {
		if (s.equals("-1")) return null;
		try {
			String h = s.startsWith("#") ? s.substring(1) : s;
			int c = (int) Long.parseLong(h, 16);
			return new float[] {
				(c >>> 24 & 0xFF) / 255F,
				(c >>> 16 & 0xFF) / 255F,
				(c >>> 8  & 0xFF) / 255F,
				(c        & 0xFF) / 255F
			};
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static boolean doRenderSky(ClientLevel level, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
		return ESSkyRenderer.renderSky(level, poseStack, projectionMatrix, partialTick, camera, setupFog);
	}
}
