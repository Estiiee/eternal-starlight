package cn.leolezury.eternalstarlight.common.block;

import cn.leolezury.eternalstarlight.common.registry.ESBlocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class VoidstoneSpikeBlock extends SpeleothemBlock {

	public VoidstoneSpikeBlock(BlockBehaviour.Properties properties) {
		super(ESBlocks.VOIDSTONE.get().defaultBlockState(), properties);
	}

	@Override
	protected int getStalactiteLandingSound() {
		return LevelEvent.SOUND_POINTED_DRIPSTONE_LAND;
	}
}