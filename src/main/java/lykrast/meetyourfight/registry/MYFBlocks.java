package lykrast.meetyourfight.registry;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.block.MYFSkullBlock;
import lykrast.meetyourfight.block.MYFSkullBlockEntity;
import lykrast.meetyourfight.block.MYFWallSkullBlock;
import lykrast.meetyourfight.misc.MYFHeads;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MYFBlocks {
	public static final DeferredRegister<Block> REG = DeferredRegister.create(ForgeRegistries.BLOCKS, MeetYourFight.MODID);
	public static final DeferredRegister<BlockEntityType<?>> REG_BE = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MeetYourFight.MODID);
	public static RegistryObject<Block> bellringerHead, bellringerHeadWall;
	public static RegistryObject<Block> fortunaHead, fortunaHeadWall;
	public static RegistryObject<Block> swampjawHead, swampjawHeadWall;
	public static RegistryObject<Block> rosalyneHead, rosalyneHeadWall, rosalyneCracked, rosalyneCrackedWall;
	
	public static RegistryObject<BlockEntityType<MYFSkullBlockEntity>> headType;
	
	static {
		bellringerHead = REG.register("bellringer_head", () -> new MYFSkullBlock(MYFHeads.BELLRINGER, skull()));
		bellringerHeadWall = REG.register("bellringer_wall_head", () -> new MYFWallSkullBlock(MYFHeads.BELLRINGER, skull().lootFrom(bellringerHead)));
		fortunaHead = REG.register("dame_fortuna_head", () -> new MYFSkullBlock(MYFHeads.DAME_FORTUNA, skull()));
		fortunaHeadWall = REG.register("dame_fortuna_wall_head", () -> new MYFWallSkullBlock(MYFHeads.DAME_FORTUNA, skull().lootFrom(fortunaHead)));
		swampjawHead = REG.register("swampjaw_head", () -> new MYFSkullBlock(MYFHeads.SWAMPJAW, skull()));
		//TODO custom block and renderer for collision and offset when on the wall (it gets buried in the wall)
		swampjawHeadWall = REG.register("swampjaw_wall_head", () -> new MYFWallSkullBlock(MYFHeads.SWAMPJAW, skull().lootFrom(swampjawHead)));
		rosalyneHead = REG.register("rosalyne_head", () -> new MYFSkullBlock(MYFHeads.ROSALYNE, skull()));
		rosalyneHeadWall = REG.register("rosalyne_wall_head", () -> new MYFWallSkullBlock(MYFHeads.ROSALYNE, skull().lootFrom(rosalyneHead)));
		rosalyneCracked = REG.register("rosalyne_head_cracked", () -> new MYFSkullBlock(MYFHeads.ROSALYNE_CRACKED, skull()));
		rosalyneCrackedWall = REG.register("rosalyne_wall_head_cracked", () -> new MYFWallSkullBlock(MYFHeads.ROSALYNE_CRACKED, skull().lootFrom(rosalyneCracked)));
		
		headType = REG_BE.register("head", () -> BlockEntityType.Builder.of(MYFSkullBlockEntity::new,
				bellringerHead.get(), bellringerHeadWall.get(),
				fortunaHead.get(), fortunaHeadWall.get(),
				swampjawHead.get(), swampjawHeadWall.get(),
				rosalyneHead.get(), rosalyneHeadWall.get(),
				rosalyneCracked.get(), rosalyneCrackedWall.get()
				).build(null));
	}
	
	private static BlockBehaviour.Properties skull() {
		return BlockBehaviour.Properties.of().strength(1).pushReaction(PushReaction.DESTROY).instrument(NoteBlockInstrument.CUSTOM_HEAD);
	}

}
