package cn.leolezury.eternalstarlight.common.mixin.client;

import cn.leolezury.eternalstarlight.common.client.handler.ESClientHandler;
import cn.leolezury.eternalstarlight.common.entity.interfaces.SpellCaster;
import cn.leolezury.eternalstarlight.common.registry.ESDataAttachments;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

	@Shadow
	protected abstract void move(double f, double g, double h);

	@WrapOperation(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", ordinal = 0))
	private void setup(Camera instance, float yRot, float xRot, Operation<Void> original) {
		Vec3 angles = ESClientHandler.onComputeCameraAngles(new Vec3(xRot, yRot, 0));
		original.call(instance, (float) angles.y, (float) angles.x);
	}
}
