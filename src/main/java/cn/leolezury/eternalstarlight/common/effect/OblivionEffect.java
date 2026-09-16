package cn.leolezury.eternalstarlight.common.effect;

import cn.leolezury.eternalstarlight.common.registry.ESAttributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class OblivionEffect extends MobEffect {

	public static final double SPEED_BONUS = 0.2;
	public static final double GRAVITY_REDUCTION = -0.2;
	public static final double FOLLOW_RANGE_REDUCTION = -0.2;

	private static final String SPEED_MODIFIER_UUID = "7e3f1a2c-9d4b-4f6a-8c3e-2b5d91a7f014";
	private static final String GRAVITY_MODIFIER_UUID = "b1d8c4e2-6a3f-4e9d-a17b-5f2c8e3d9a67";
	private static final String FOLLOW_RANGE_MODIFIER_UUID = "4c9a7e21-f3d6-4b8a-9e1c-7a3f5d2b8c90";

	//TODO add handling for the gravity attribute
	public OblivionEffect(MobEffectCategory category, int color) {
		super(category, color);
		this.addAttributeModifier(Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_UUID, SPEED_BONUS, AttributeModifier.Operation.MULTIPLY_TOTAL);
		//this.addAttributeModifier(ESAttributes.GRAVITY.get(), GRAVITY_MODIFIER_UUID, GRAVITY_REDUCTION, AttributeModifier.Operation.MULTIPLY_TOTAL);
		this.addAttributeModifier(ESAttributes.ENEMY_FOLLOW_RANGE_MULTIPLIER.get(), FOLLOW_RANGE_MODIFIER_UUID, FOLLOW_RANGE_REDUCTION, AttributeModifier.Operation.ADDITION);
	}
}