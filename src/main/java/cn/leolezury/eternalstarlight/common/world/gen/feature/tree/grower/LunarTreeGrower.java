package cn.leolezury.eternalstarlight.common.world.gen.feature.tree.grower;

import cn.leolezury.eternalstarlight.common.data.ESConfiguredFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import javax.annotation.Nullable;

public class LunarTreeGrower extends AbstractMegaTreeGrower {

	@Nullable
	@Override
	protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean bees) {
		return ESConfiguredFeatures.LUNAR_COLORED;
	}

	@Nullable
	@Override
	protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredMegaFeature(RandomSource random) {
		return ESConfiguredFeatures.LUNAR_COLORED_HUGE;
	}
}
