package gregtech.common.blocks.wood;

import gregtech.api.GregTechAPI;
import gregtech.common.blocks.wood.BlockGregLog.LogVariant;
import gregtech.common.worldgen.WorldGenRubberTree;
import net.minecraft.block.*;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

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
            if (worldIn.getLightFromNeighbors(pos.up()) >= 9 && rand.nextInt(30) == 0) {
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
    public void grow(@Nonnull World worldIn, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        new WorldGenRubberTree(true).grow(worldIn, pos, rand);
    }

    public ItemStack getItem(LogVariant variant) {
        return new ItemStack(this, 1, variant.ordinal() * 2);
    }

    public ItemStack getItem(LogVariant variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal() * 2);
    }
}
