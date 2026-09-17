package cn.leolezury.eternalstarlight.common.block;

import cn.leolezury.eternalstarlight.common.registry.ESBlocks;
import cn.leolezury.eternalstarlight.common.registry.ESItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SacredLanternvinePlantBlock extends GrowingPlantBodyBlock {

	public SacredLanternvinePlantBlock(Properties properties) {
		super(properties, Direction.UP, SacredLanternvineBlock.SHAPE, false);
	}

	@Override
	public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
		return ESItems.SACRED_LANTERNVINE.get().getDefaultInstance();
	}

	@Override
	protected GrowingPlantHeadBlock getHeadBlock() {
		return ESBlocks.SACRED_LANTERNVINE.get();
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos attachPos = pos.relative(this.growthDirection.getOpposite());
		BlockState attachState = level.getBlockState(attachPos);
		if (!this.canAttachTo(attachState)) {
			return false;
		} else {
			return attachState.is(this.getHeadBlock()) || attachState.is(this.getBodyBlock()) || attachState.isFaceSturdy(level, attachPos, this.growthDirection);
		}
	}
}