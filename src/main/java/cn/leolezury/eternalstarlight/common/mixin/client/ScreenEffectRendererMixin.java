package cn.leolezury.eternalstarlight.common.mixin.client;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.handler.ESClientHandler;
import cn.leolezury.eternalstarlight.common.registry.ESDataAttachments;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {
	@Unique
	private static final Material ABYSSAL_FIRE_1 = new Material(TextureAtlas.LOCATION_BLOCKS, EternalStarlight.id("block/abyssal_fire_1"));

	@WrapOperation(
		method = "renderScreenEffect",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;renderTex(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lcom/mojang/blaze3d/vertex/PoseStack;)V")
	)
	private static void renderTex(
		TextureAtlasSprite texture, PoseStack poseStack, Operation<Void> original,
		@Local(name = "overlay") Pair<BlockState, BlockPos> overlay
	) {
		BlockState state = overlay.getLeft();
		boolean allow = ESClientHandler.onRenderBlockOverlay(Minecraft.getInstance().player, state);
		if (allow) {
			original.call(texture, poseStack);
		}
	}
	@Inject(method = "renderScreenEffect", at = @At(value = "TAIL"))
	private static void renderScreenEffect(Minecraft minecraft, PoseStack poseStack, CallbackInfo ci) {
		if (minecraft.player != null && !minecraft.player.isSpectator()) {
			if (ESDataAttachments.ABYSSAL_FIRE_TICKS.getData(minecraft.player) > 0) {
				renderAbyssalFlame(poseStack);
			}
		}
	}

	@Unique
	private static void renderAbyssalFlame(PoseStack poseStack) {
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.depthFunc(519);
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();

		TextureAtlasSprite sprite = ABYSSAL_FIRE_1.sprite();
		RenderSystem.setShaderTexture(0, sprite.atlasLocation());

		float u0 = sprite.getU0();
		float u1 = sprite.getU1();
		float v0 = sprite.getV0();
		float v1 = sprite.getV1();

		float midU = (u0 + u1) * 0.5f;
		float midV = (v0 + v1) * 0.5f;
		float shrink = sprite.uvShrinkRatio();

		float uMin = Mth.lerp(shrink, u0, midU);
		float uMax = Mth.lerp(shrink, u1, midU);
		float vMin = Mth.lerp(shrink, v0, midV);
		float vMax = Mth.lerp(shrink, v1, midV);

		for (int r = 0; r < 2; r++) {
			poseStack.pushPose();
			poseStack.translate((-(r * 2 - 1)) * 0.24F, -0.3F, 0.0F);
			poseStack.mulPose(Axis.YP.rotationDegrees((r * 2 - 1) * 10.0F));

			Matrix4f mat = poseStack.last().pose();

			BufferBuilder buf = Tesselator.getInstance().getBuilder();
			buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

			buf.vertex(mat, -0.5F, -0.5F, -0.5F).uv(uMax, vMax).color(1f, 1f, 1f, 0.9f).endVertex();
			buf.vertex(mat,  0.5F, -0.5F, -0.5F).uv(uMin, vMax).color(1f, 1f, 1f, 0.9f).endVertex();
			buf.vertex(mat,  0.5F,  0.5F, -0.5F).uv(uMin, vMin).color(1f, 1f, 1f, 0.9f).endVertex();
			buf.vertex(mat, -0.5F,  0.5F, -0.5F).uv(uMax, vMin).color(1f, 1f, 1f, 0.9f).endVertex();

			BufferUploader.drawWithShader(buf.end());

			poseStack.popPose();
		}

		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
		RenderSystem.depthFunc(515);
	}
}
