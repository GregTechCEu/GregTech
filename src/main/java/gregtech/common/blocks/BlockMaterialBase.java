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

import org.jetbrains.annotations.NotNull;

public abstract class BlockMaterialBase extends Block {

    public BlockMaterialBase(net.minecraft.block.material.Material material) {
        super(material);
    }

    @NotNull
    public ItemStack getItem(@NotNull Material material) {
        return GTUtility.toItem(getDefaultState().withProperty(getVariantProperty(), material));
    }

    @NotNull
    public Material getGtMaterial(int meta) {
        if (meta >= getVariantProperty().getAllowedValues().size()) {
            meta = 0;
        }
        return getVariantProperty().getAllowedValues().get(meta);
    }

    @NotNull
    public Material getGtMaterial(@NotNull ItemStack stack) {
        return getGtMaterial(stack.getMetadata());
    }

    @NotNull
    public Material getGtMaterial(@NotNull IBlockState state) {
        return state.getValue(getVariantProperty());
    }

    @NotNull
    public IBlockState getBlock(@NotNull Material material) {
        return getDefaultState().withProperty(getVariantProperty(), material);
    }

    @NotNull
    public abstract PropertyMaterial getVariantProperty();

    @NotNull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, getVariantProperty());
    }

    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(getVariantProperty(), getGtMaterial(meta));
    }

    @Override
    public int getMetaFromState(@NotNull IBlockState state) {
        return getVariantProperty().getAllowedValues().indexOf(state.getValue(getVariantProperty()));
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public void getSubBlocks(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> list) {
        for (IBlockState state : blockState.getValidStates()) {
            if (getGtMaterial(state) != Materials.NULL) {
                list.add(GTUtility.toItem(state));
            }
        }
    }

    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public MapColor getMapColor(@NotNull IBlockState state, @NotNull IBlockAccess worldIn, @NotNull BlockPos pos) {
        return getMaterial(state).getMaterialMapColor();
    }

    @Override
    public int getFlammability(@NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EnumFacing face) {
        Material material = getGtMaterial(world.getBlockState(pos));
        if (material.hasFlag(MaterialFlags.FLAMMABLE)) {
            return 20; // flammability of things like Wood Planks
        }
        return super.getFlammability(world, pos, face);
    }

    @Override
    public int getFireSpreadSpeed(@NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EnumFacing face) {
        Material material = getGtMaterial(world.getBlockState(pos));
        if (material.hasFlag(MaterialFlags.FLAMMABLE)) {
            return 5; // encouragement of things like Wood Planks
        }
        return super.getFireSpreadSpeed(world, pos, face);
    }
}
