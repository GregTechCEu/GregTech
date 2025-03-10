package gregtech.common.blocks.wood;

import gregtech.common.creativetab.GTCreativeTabs;
import gregtech.worldgen.impl.WorldGenRubberTree;

import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.event.terraingen.TerrainGen;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static net.minecraft.block.BlockSapling.STAGE;

public class BlockRubberSapling extends BlockBush implements IGrowable {

    private static final AxisAlignedBB SAPLING_AABB = new AxisAlignedBB(0.1, 0.0D, 0.1, 0.9, 0.8, 0.9);

    public BlockRubberSapling() {
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(STAGE, 0));
        setTranslationKey("rubber_sapling");
        setCreativeTab(GTCreativeTabs.TAB_GREGTECH);
        setHardness(0.0F);
        setSoundType(SoundType.PLANT);
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, STAGE);
    }

    @Override
    public void updateTick(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state,
                           @NotNull Random rand) {
        if (!worldIn.isRemote) {
            super.updateTick(worldIn, pos, state, rand);
            if (!worldIn.isAreaLoaded(pos, 1)) {
                return;
            }

            // longer than the vanilla growth requirement
            if (worldIn.getLightFromNeighbors(pos.up()) >= 9 && rand.nextInt(30) == 0) {
                if (state.getValue(STAGE) == 0) {
                    worldIn.setBlockState(pos, state.cycleProperty(STAGE), 4);
                } else {
                    this.grow(worldIn, rand, pos, state);
                }
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
    public int getMetaFromState(@NotNull IBlockState state) {
        return state.getValue(STAGE) << 3;
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull AxisAlignedBB getBoundingBox(@NotNull IBlockState state, @NotNull IBlockAccess source,
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
        if (!TerrainGen.saplingGrowTree(worldIn, rand, pos)) {
            return;
        }

        worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 4);
        if (!WorldGenRubberTree.INSTANCE_NOTIFY.generate(worldIn, rand, pos)) {
            worldIn.setBlockState(pos, state, 4);
        }
    }

    @Override
    public @NotNull EnumPlantType getPlantType(@NotNull IBlockAccess world, @NotNull BlockPos pos) {
        return EnumPlantType.Plains;
    }
}
