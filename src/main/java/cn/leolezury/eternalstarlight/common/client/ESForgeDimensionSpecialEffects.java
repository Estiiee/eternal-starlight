package cn.leolezury.eternalstarlight.common.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import org.joml.Matrix4f;

public class ESForgeDimensionSpecialEffects extends ESDimensionSpecialEffects {

	public ESForgeDimensionSpecialEffects(float cloudHeight, boolean placebo, SkyType fogType, boolean brightenLightMap, boolean entityLightingBottomsLit) {
		super(cloudHeight, placebo, fogType, brightenLightMap, entityLightingBottomsLit);
	}

	@Override
	public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
		return ESDimensionSpecialEffects.doRenderSky(
			level, partialTick, poseStack, camera, projectionMatrix, isFoggy, setupFog
		);
	}
}

