package gregtech.common.blocks.wood;

import gregtech.api.GregTechAPI;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.wood.BlockGregLog.LogVariant;
import net.minecraft.block.*;
import net.minecraft.block.BlockLog.EnumAxis;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenTrees;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.terraingen.TerrainGen;

import javax.annotation.Nonnull;
import java.util.Random;

public class BlockGregSapling extends BlockBush implements IGrowable, IPlantable {

    public static final PropertyEnum<LogVariant> VARIANT = PropertyEnum.create("variant", LogVariant.class);
    public static final PropertyInteger STAGE = PropertyInteger.create("stage", 0, 1);
    protected static final AxisAlignedBB SAPLING_AABB = new AxisAlignedBB(0.1, 0.0D, 0.1, 0.9, 0.8, 0.9);

    public BlockGregSapling() {
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(VARIANT, LogVariant.RUBBER_WOOD)
                .withProperty(STAGE, 0));
        setTranslationKey("gt.sapling");
        setCreativeTab(GregTechAPI.TAB_GREGTECH);
        setHardness(0.0F);
        setSoundType(SoundType.PLANT);
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, VARIANT, STAGE);
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(VARIANT, LogVariant.values()[meta % 4 % LogVariant.values().length])
                .withProperty(STAGE, meta / 4 % 2);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(STAGE) * 4 + state.getValue(VARIANT).ordinal();
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    @Override
    public void getSubBlocks(@Nonnull CreativeTabs itemIn, @Nonnull NonNullList<ItemStack> items) {
        for (LogVariant logVariant : LogVariant.values()) {
            items.add(new ItemStack(this, 1, logVariant.ordinal()));
        }
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
        return SAPLING_AABB;
    }

    @Override
    public void updateTick(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random rand) {
        if (!worldIn.isRemote) {
            super.updateTick(worldIn, pos, state, rand);
            if (!worldIn.isAreaLoaded(pos, 1))
                return;
            if (worldIn.getLightFromNeighbors(pos.up()) >= 9 && rand.nextInt(7) == 0) {
                this.grow(worldIn, rand, pos, state);
            }
        }
    }

    @Override
    public boolean canGrow(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean canUseBonemeal(World worldIn, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        return (double) worldIn.rand.nextFloat() < 0.45D;
    }

    @Nonnull
    @Override
    public EnumPlantType getPlantType(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        return EnumPlantType.Plains;
    }

    @Override
    public void grow(@Nonnull World worldIn, @Nonnull Random rand, @Nonnull BlockPos pos, IBlockState state) {
        if (state.getValue(STAGE) == 0) {
            worldIn.setBlockState(pos, state.cycleProperty(STAGE), 4);
        } else {
            this.generateTree(worldIn, pos, state, rand);
        }
    }

    public void generateTree(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!TerrainGen.saplingGrowTree(worldIn, rand, pos)) return;
        WorldGenerator worldgenerator;
        IBlockState logState = MetaBlocks.LOG.getDefaultState()
                .withProperty(BlockGregLog.VARIANT, LogVariant.RUBBER_WOOD)
                .withProperty(BlockGregLog.NATURAL, true);
        IBlockState leavesState = MetaBlocks.LEAVES.getDefaultState()
                .withProperty(BlockGregLeaves.VARIANT, LogVariant.RUBBER_WOOD);
        if (rand.nextInt(10) == 0) {
            worldgenerator = new WorldGenBigTreeCustom(true, logState, leavesState.withProperty(BlockGregLeaves.CHECK_DECAY, false), BlockGregLog.LOG_AXIS);
        } else {
            worldgenerator = new WorldGenTrees(true, 6, logState, leavesState, false);
        }
        worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 4);
        if (!worldgenerator.generate(worldIn, rand, pos)) {
            worldIn.setBlockState(pos, state, 4);
        }
    }

    public ItemStack getItem(LogVariant variant) {
        return new ItemStack(this, 1, variant.ordinal() * 2);
    }

    public ItemStack getItem(LogVariant variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal() * 2);
    }

    public static class WorldGenBigTreeCustom extends WorldGenBigTree {

        private final IBlockState logBlock;
        private final IBlockState leavesBlock;
        private final PropertyEnum<EnumAxis> logAxisProperty;

        public WorldGenBigTreeCustom(boolean notify, IBlockState logBlock, IBlockState leavesBlock, PropertyEnum<EnumAxis> logAxisProperty) {
            super(notify);
            this.logBlock = logBlock;
            this.leavesBlock = leavesBlock;
            this.logAxisProperty = logAxisProperty;
        }

        @Override
        protected void setBlockAndNotifyAdequately(@Nonnull World worldIn, @Nonnull BlockPos pos, IBlockState state) {
            if (state.getBlock() instanceof BlockLeaves) {
                state = leavesBlock;
            } else if (state.getBlock() instanceof BlockLog) {
                EnumAxis rotation = state.getValue(BlockLog.LOG_AXIS);
                state = logBlock.withProperty(logAxisProperty, rotation);
            }
            super.setBlockAndNotifyAdequately(worldIn, pos, state);
        }
    }

}
