package gregtech.common.blocks.foam;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.OreDictUnifier;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class BlockFoam extends BlockColored {

    private final boolean isReinforced;

    public BlockFoam(boolean isReinforced) {
        super(Material.SAND);
        setTranslationKey(isReinforced ? "gt.reinforced_foam" : "gt.foam");
        setSoundType(SoundType.SNOW);
        setResistance(0.3f);
        setHardness(0.5f);
        setLightOpacity(0);
        setTickRandomly(true);
        setCreativeTab(GregTechAPI.TAB_GREGTECH);
        this.isReinforced = isReinforced;
    }

    @Override
    public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stackInHand = playerIn.getHeldItem(hand);
        if (!stackInHand.isEmpty() && OreDictUnifier.getOreDictionaryNames(stackInHand).contains("sand")) {
            worldIn.setBlockState(pos, getPetrifiedBlock(state));
            worldIn.playSound(playerIn, pos, SoundEvents.BLOCK_SAND_PLACE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            if (!playerIn.capabilities.isCreativeMode)
                stackInHand.shrink(1);
            return true;
        }
        return false;
    }

    @Override
    public void randomTick(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, Random random) {
        int lightLevel = (worldIn.canSeeSky(pos) && worldIn.isDaytime()) ? 16 : worldIn.getLight(pos);
        if (random.nextInt(20 - lightLevel) == 0) {
            worldIn.setBlockState(pos, getPetrifiedBlock(state));
        }
    }

    private IBlockState getPetrifiedBlock(IBlockState state) {
        Block block = isReinforced ? MetaBlocks.REINFORCED_PETRIFIED_FOAM : MetaBlocks.PETRIFIED_FOAM;
        return block.getDefaultState().withProperty(COLOR, state.getValue(COLOR));
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public EnumPushReaction getPushReaction(@Nonnull IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    @Nullable
    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getCollisionBoundingBox(@Nonnull IBlockState blockState, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
        return null;
    }

    @Nonnull
    @Override
    public Item getItemDropped(@Nonnull IBlockState state, @Nonnull Random rand, int fortune) {
        return Items.AIR;
    }

    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(@Nonnull IBlockState state) {
        return false;
    }


    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(@Nonnull IBlockState state) {
        return false;
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }
}
