package cn.leolezury.eternalstarlight.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class StickyEffect extends MobEffect {

	public static final double SPEED_PENALTY = -0.06;

	private static final String SPEED_MODIFIER_UUID = "3a8e5c14-7b2d-4f91-a6c3-e9d24f7b8351";

	public StickyEffect(MobEffectCategory category, int color) {
		super(category, color);
		this.addAttributeModifier(Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_UUID, SPEED_PENALTY, AttributeModifier.Operation.MULTIPLY_TOTAL);
	}
}