package lykrast.meetyourfight.block;

import java.util.Map;

import com.google.common.collect.Maps;

import lykrast.meetyourfight.misc.MYFHeads;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MYFWallSkullBlock extends WallSkullBlock {
	private static final Map<Direction, VoxelShape> SWAMPJAW = Maps.immutableEnumMap(Map.of(
			Direction.NORTH, Block.box(3, 4, 6, 13, 12, 16),
			Direction.SOUTH, Block.box(3, 4, 0, 13, 12, 10),
			Direction.EAST, Block.box(0, 4, 3, 10, 12, 13),
			Direction.WEST, Block.box(6, 4, 3, 16, 12, 13)
			));

	public MYFWallSkullBlock(SkullBlock.Type type, Properties properties) {
		super(type, properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new MYFSkullBlockEntity(pos, state);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return getType() == MYFHeads.SWAMPJAW ? SWAMPJAW.get(state.getValue(FACING)) : super.getShape(state, level, pos, context);
	}

}
