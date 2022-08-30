package gregtech.api.block;

import codechicken.lib.block.property.unlisted.UnlistedBooleanProperty;
import gregtech.api.util.GTUtility;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nonnull;

public class VariantActiveBlock<T extends Enum<T> & IStringSerializable> extends VariantBlock<T>{
    public static final UnlistedBooleanProperty ACTIVE = new UnlistedBooleanProperty("active");

    public VariantActiveBlock(Material materialIn) {
        super(materialIn);
    }

    @Override
    public IBlockState getState(T variant) {
        return super.getState(variant);
    }

    public IBlockState getState(T variant, boolean active) {
        return super.getState(variant);
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
        return new BlockStateContainer.Builder(this).add(VARIANT).add(ACTIVE).build();
    }

    @Override
    public IExtendedBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState ext = (IExtendedBlockState) state;
        //TileEntity te = world.getTileEntity(pos);
        //if (te instanceof V) {
            //ext = ext.withProperty(UNLISTED_PROP, ((MyTE) te).getSomeImmutableData());
        //}
        return ext;
    }

    @Override
    public int damageDropped(@Nonnull IBlockState state) {
        return getMetaFromState(state);
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return super.getStateFromMeta(meta);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return super.getMetaFromState(state);
    }
}
