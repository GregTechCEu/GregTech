package gregtech.common.blocks.wood;

import gregtech.api.GregTechAPI;
import gregtech.common.worldgen.WorldGenRubberTree;

import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static net.minecraft.block.BlockSapling.STAGE;

public class BlockRubberSapling extends BlockBush implements IGrowable {

    protected static final AxisAlignedBB SAPLING_AABB = new AxisAlignedBB(0.1, 0.0D, 0.1, 0.9, 0.8, 0.9);

    public BlockRubberSapling() {
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(STAGE, 0));
        setTranslationKey("rubber_sapling");
        setCreativeTab(GregTechAPI.TAB_GREGTECH);
        setHardness(0.0F);
        setSoundType(SoundType.PLANT);
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, STAGE);
    }

    @Override
    public void updateTick(World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull Random rand) {
        if (!worldIn.isRemote) {
            super.updateTick(worldIn, pos, state, rand);
            if (!worldIn.isAreaLoaded(pos, 1))
                return;
            if (worldIn.getLightFromNeighbors(pos.up()) >= 9 && rand.nextInt(30) == 0) {
                this.grow(worldIn, rand, pos, state);
            }
        }
    }

    @Override
    @NotNull
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(STAGE, (meta & 8) >> 3);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;
        i |= state.getValue(STAGE) << 3;
        return i;
    }

    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getBoundingBox(@NotNull IBlockState state, @NotNull IBlockAccess source,
                                        @NotNull BlockPos pos) {
        return SAPLING_AABB;
    }

    @Override
    public boolean canGrow(@NotNull World world, @NotNull BlockPos blockPos, @NotNull IBlockState iBlockState,
                           boolean b) {
        return true;
    }

    @Override
    public boolean canUseBonemeal(@NotNull World world, @NotNull Random random, @NotNull BlockPos blockPos,
                                  @NotNull IBlockState iBlockState) {
        return true;
    }

    @Override
    public boolean canBeReplacedByLeaves(@NotNull IBlockState state, @NotNull IBlockAccess world,
                                         @NotNull BlockPos pos) {
        return true;
    }

    @Override
    public void grow(@NotNull World worldIn, @NotNull Random rand, @NotNull BlockPos pos, @NotNull IBlockState state) {
        WorldGenRubberTree.TREE_GROW_INSTANCE.grow(worldIn, pos, rand);
    }

    @Override
    @NotNull
    public EnumPlantType getPlantType(@NotNull IBlockAccess world, @NotNull BlockPos pos) {
        return EnumPlantType.Plains;
    }
}
