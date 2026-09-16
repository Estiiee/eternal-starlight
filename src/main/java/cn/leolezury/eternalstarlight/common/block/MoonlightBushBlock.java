package cn.leolezury.eternalstarlight.common.block;

import cn.leolezury.eternalstarlight.common.registry.ESItems;
import cn.leolezury.eternalstarlight.common.registry.ESParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class MoonlightBushBlock extends ShortBushBlock implements BonemealableBlock {
	public static final BooleanProperty BERRIES = BlockStateProperties.BERRIES;

	public MoonlightBushBlock(Properties properties) {
		super(11, properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(BERRIES, false));
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		boolean berries = blockState.getValue(BERRIES);
		if (!berries && randomSource.nextInt(15) == 0 && serverLevel.getRawBrightness(blockPos.above(), 0) >= 9) {
			BlockState state = blockState.setValue(BERRIES, true);
			serverLevel.setBlockAndUpdate(blockPos, state);
			serverLevel.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(state));
		}
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

		ItemStack itemStack = player.getItemInHand(hand);
		boolean berries = state.getValue(BERRIES);

		if (!berries && itemStack.is(Items.BONE_MEAL)) {
			return InteractionResult.PASS;
		}

		if (berries) {
			popResource(level, pos, ESItems.LUNAR_BERRIES.get().getDefaultInstance());
			BlockState newState = state.setValue(BERRIES, false);
			level.setBlockAndUpdate(pos, newState);
			level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newState));
			return InteractionResult.sidedSuccess(level.isClientSide);
		}

		return InteractionResult.PASS;
	}


	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BERRIES);
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClientSide) {
		return !state.getValue(BERRIES);
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		serverLevel.setBlockAndUpdate(blockPos, blockState.setValue(BERRIES, true));
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextInt(4) == 0) {

			int count = random.nextInt(1, 4);
			var particle = random.nextInt(4) == 0
				? ESParticles.FIREFLY.get()
				: ESParticles.STARDUST.get();

			for (int i = 0; i < count; i++) {
				double x = pos.getX() + random.nextDouble();
				double y = pos.getY() + random.nextDouble();
				double z = pos.getZ() + random.nextDouble();
				level.addParticle(particle, x, y, z, 0, 0, 0);
			}
		}
	}

}
