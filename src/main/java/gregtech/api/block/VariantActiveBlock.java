package gregtech.api.block;

import gregtech.api.util.GTUtility;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public class VariantActiveBlock<T extends Enum<T> & IStringSerializable> extends VariantBlock<T>{
    public static final PropertyBool ACTIVE = PropertyBool.create("active");

    public VariantActiveBlock(Material materialIn) {
        super(materialIn);
    }

    @Override
    public IBlockState getState(T variant) {
        return super.getState(variant).withProperty(ACTIVE, false);
    }

    public IBlockState getState(T variant, boolean active) {
        return super.getState(variant).withProperty(ACTIVE, active);
    }

    public boolean getActive(IBlockState blockState) {
        return blockState.getValue(ACTIVE);
    }

    public boolean getActive(ItemStack stack) {
        return getActive(getStateFromMeta(stack.getItemDamage()));
    }

    @Override
    @SuppressWarnings("deprecation")
    protected boolean canSilkHarvest() {
        return false;
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        Class<T> enumClass = GTUtility.getActualTypeParameter(getClass(), VariantActiveBlock.class, 0);
        this.VARIANT = PropertyEnum.create("variant", enumClass);
        this.VALUES = enumClass.getEnumConstants();
        return new BlockStateContainer(this, VARIANT, ACTIVE);
    }

    @Override
    public int damageDropped(@Nonnull IBlockState state) {
        return getMetaFromState(state) - (state.getValue(ACTIVE) ? 8 : 0);
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return super.getStateFromMeta(meta).withProperty(ACTIVE, meta / 8 >= 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return super.getMetaFromState(state) + (state.getValue(ACTIVE) ? 8 : 0);
    }
}
