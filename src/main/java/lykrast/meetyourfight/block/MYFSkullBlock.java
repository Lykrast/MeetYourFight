package lykrast.meetyourfight.block;

import lykrast.meetyourfight.misc.MYFHeads;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MYFSkullBlock extends SkullBlock {
	public MYFSkullBlock(SkullBlock.Type type, Properties properties) {
		super(type, properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new MYFSkullBlockEntity(pos, state);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return getType() == MYFHeads.SWAMPJAW ? PIGLIN_SHAPE : super.getShape(state, level, pos, context);
	}

}
