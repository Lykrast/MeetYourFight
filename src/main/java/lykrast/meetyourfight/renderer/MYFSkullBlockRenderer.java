package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;

import lykrast.meetyourfight.misc.MYFHeads;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;

public class MYFSkullBlockRenderer extends SkullBlockRenderer {
	public static SkullModelBase swampjawModel;

	public MYFSkullBlockRenderer(Context context) {
		super(context);
	}
	
	@Override
	public void render(SkullBlockEntity tile, float partialTicks, PoseStack pose, MultiBufferSource buffer, int p_112538_, int p_112539_) {
		//copy pasting the super model but only for swampjaw so that I can change the offset
		//the fucking modelByType is private aaaaa
		BlockState blockstate = tile.getBlockState();
		SkullBlock.Type skull = ((AbstractSkullBlock) blockstate.getBlock()).getType();
		if (skull == MYFHeads.SWAMPJAW) {
			float animation = tile.getAnimation(partialTicks);
			boolean isWall = blockstate.getBlock() instanceof WallSkullBlock;
			Direction direction = isWall ? blockstate.getValue(WallSkullBlock.FACING) : null;
			int rotationIndex = isWall ? RotationSegment.convertToSegment(direction.getOpposite()) : blockstate.getValue(SkullBlock.ROTATION);
			float rotation = RotationSegment.convertToDegrees(rotationIndex);
			RenderType rendertype = getRenderType(skull, tile.getOwnerProfile());
			//swampjaw skull is 10x10 on x/z and the wall offset is for 8x8
			//need to move opposite direction 1 model px = 1/16 of a block, and luckily the renderskull start with the translation
			pose.pushPose();
			if (direction != null) pose.translate(direction.getStepX() * 1/16f, 0, direction.getStepZ() * 1/16f);
			renderSkull(direction, rotation, animation, pose, buffer, p_112538_, swampjawModel, rendertype);
			pose.popPose();
		}
		else super.render(tile, partialTicks, pose, buffer, p_112538_, p_112539_);
	}

}
