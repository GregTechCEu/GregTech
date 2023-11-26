package gregtech.common.blocks;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.util.GTUtility;
import gregtech.common.blocks.properties.PropertyMaterial;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

public abstract class BlockMaterialBase extends Block {

    public BlockMaterialBase(net.minecraft.block.material.Material material) {
        super(material);
    }

    @Nonnull
    public ItemStack getItem(@Nonnull Material material) {
        return GTUtility.toItem(getDefaultState().withProperty(getVariantProperty(), material));
    }

    @Nonnull
    public Material getGtMaterial(int meta) {
        if (meta >= getVariantProperty().getAllowedValues().size()) {
            meta = 0;
        }
        return getVariantProperty().getAllowedValues().get(meta);
    }

    @Nonnull
    public Material getGtMaterial(@Nonnull ItemStack stack) {
        return getGtMaterial(stack.getMetadata());
    }

    @Nonnull
    public Material getGtMaterial(@Nonnull IBlockState state) {
        return state.getValue(getVariantProperty());
    }

    @Nonnull
    public IBlockState getBlock(@Nonnull Material material) {
        return getDefaultState().withProperty(getVariantProperty(), material);
    }

    @Nonnull
    public abstract PropertyMaterial getVariantProperty();

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, getVariantProperty());
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(getVariantProperty(), getGtMaterial(meta));
    }

    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
        return getVariantProperty().getAllowedValues().indexOf(state.getValue(getVariantProperty()));
    }

    @Override
    public int damageDropped(@Nonnull IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public void getSubBlocks(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> list) {
        for (IBlockState state : blockState.getValidStates()) {
            if (getGtMaterial(state) != Materials.NULL) {
                list.add(GTUtility.toItem(state));
            }
        }
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public MapColor getMapColor(@Nonnull IBlockState state, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
        return getMaterial(state).getMaterialMapColor();
    }

    @Override
    public int getFlammability(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        Material material = getGtMaterial(world.getBlockState(pos));
        if (material.hasFlag(MaterialFlags.FLAMMABLE)) {
            return 20; // flammability of things like Wood Planks
        }
        return super.getFlammability(world, pos, face);
    }

    @Override
    public int getFireSpreadSpeed(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        Material material = getGtMaterial(world.getBlockState(pos));
        if (material.hasFlag(MaterialFlags.FLAMMABLE)) {
            return 5; // encouragement of things like Wood Planks
        }
        return super.getFireSpreadSpeed(world, pos, face);
    }
}
