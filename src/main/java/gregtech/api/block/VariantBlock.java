package gregtech.api.block;

import gregtech.api.GregTechAPI;
import gregtech.api.util.GTUtility;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class VariantBlock<T extends Enum<T> & IStringSerializable> extends Block {

    protected PropertyEnum<T> VARIANT;
    protected T[] VALUES;

    public VariantBlock(Material materialIn) {
        super(materialIn);
        if (VALUES.length > 0 && VALUES[0] instanceof IStateHarvestLevel) {
            for (T t : VALUES) {
                IStateHarvestLevel stateHarvestLevel = (IStateHarvestLevel) t;
                IBlockState state = getState(t);
                setHarvestLevel(stateHarvestLevel.getHarvestTool(state), stateHarvestLevel.getHarvestLevel(state), state);
            }
        }
        setCreativeTab(GregTechAPI.TAB_GREGTECH);
        setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, VALUES[0]));
    }

    @Override
    public void getSubBlocks(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> list) {
        for (T variant : VALUES) {
            list.add(getItemVariant(variant));
        }
    }

    public IBlockState getState(T variant) {
        return getDefaultState().withProperty(VARIANT, variant);
    }

    public T getState(IBlockState blockState) {
        return blockState.getValue(VARIANT);
    }

    public T getState(ItemStack stack) {
        return getState(getStateFromMeta(stack.getItemDamage()));
    }

    public ItemStack getItemVariant(T variant) {
        return getItemVariant(variant, 1);
    }

    public ItemStack getItemVariant(T variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        Class<T> enumClass = GTUtility.getActualTypeParameter(getClass(), VariantBlock.class, 0);
        this.VARIANT = PropertyEnum.create("variant", enumClass);
        this.VALUES = enumClass.getEnumConstants();
        return new BlockStateContainer(this, VARIANT);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World player, List<String> tooltip, @Nonnull ITooltipFlag advanced) {
        //basic tooltip for all variant blocks
        tooltip.add(I18n.format("tile.machine_casing.tooltip1"));
        tooltip.add(I18n.format("tile.machine_casing.tooltip2"));
        //tier less tooltip like: tile.turbine_casing.tooltip
        String unlocalizedVariantTooltip = getTranslationKey() + ".tooltip";
        if (I18n.hasKey(unlocalizedVariantTooltip))
            tooltip.addAll(Arrays.asList(I18n.format(unlocalizedVariantTooltip).split("/n")));
        //item specific tooltip: tile.turbine_casing.bronze_gearbox.tooltip
        String unlocalizedTooltip = stack.getTranslationKey() + ".tooltip";
        if (I18n.hasKey(unlocalizedTooltip)) tooltip.addAll(Arrays.asList(I18n.format(unlocalizedTooltip).split("/n")));
    }

    @Override
    public int damageDropped(@Nonnull IBlockState state) {
        return getMetaFromState(state);
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(VARIANT, VALUES[meta % VALUES.length]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

}
