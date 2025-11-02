package gregtech.api.block;

import gregtech.api.util.LocalizationUtils;
import gregtech.common.creativetab.GTCreativeTabs;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class VariantBlock<T extends Enum<T> & IStringSerializable> extends Block {

    protected PropertyEnum<T> VARIANT;
    protected T[] VALUES;

    public VariantBlock(@NotNull Material materialIn) {
        super(materialIn);
        if (VALUES.length > 0 && VALUES[0] instanceof IStateHarvestLevel) {
            for (T t : VALUES) {
                IStateHarvestLevel stateHarvestLevel = (IStateHarvestLevel) t;
                IBlockState state = getState(t);
                setHarvestLevel(stateHarvestLevel.getHarvestTool(state), stateHarvestLevel.getHarvestLevel(state),
                        state);
            }
        }
        setCreativeTab(GTCreativeTabs.TAB_GREGTECH);
        setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, VALUES[0]));
    }

    @Override
    public void getSubBlocks(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> list) {
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

    @NotNull
    @Override
    protected BlockStateContainer createBlockState() {
        Class<T> enumClass = getActualTypeParameter(getClass(), VariantBlock.class);
        this.VARIANT = PropertyEnum.create("variant", enumClass);
        this.VALUES = enumClass.getEnumConstants();
        return new BlockStateContainer(this, VARIANT);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag advanced) {
        // tier less tooltip like: tile.turbine_casing.tooltip
        String unlocalizedVariantTooltip = getTranslationKey() + ".tooltip";
        if (I18n.hasKey(unlocalizedVariantTooltip))
            Collections.addAll(tooltip, LocalizationUtils.formatLines(unlocalizedVariantTooltip));
        // item specific tooltip: tile.turbine_casing.bronze_gearbox.tooltip
        String unlocalizedTooltip = stack.getTranslationKey() + ".tooltip";
        if (I18n.hasKey(unlocalizedTooltip))
            Collections.addAll(tooltip, LocalizationUtils.formatLines(unlocalizedTooltip));
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return getMetaFromState(state);
    }

    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(VARIANT, VALUES[meta % VALUES.length]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    @NotNull
    @Override
    public SoundType getSoundType(@NotNull IBlockState state, @NotNull World world, @NotNull BlockPos pos,
                                  @Nullable Entity entity) {
        if (getState(state) instanceof IStateSoundType stateSoundType) {
            return stateSoundType.getSoundType(state);
        }
        return super.getSoundType(state, world, pos, entity);
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        if (getState(state) instanceof IStateSpawnControl stateSpawnControl) {
            return stateSpawnControl.canCreatureSpawn(state, world, pos, type);
        }
        return super.canCreatureSpawn(state, world, pos, type);
    }

    // magic is here
    @SuppressWarnings("unchecked")
    protected static <T, R> Class<T> getActualTypeParameter(Class<? extends R> thisClass, Class<R> declaringClass) {
        Type type = thisClass.getGenericSuperclass();

        while (!(type instanceof ParameterizedType) || ((ParameterizedType) type).getRawType() != declaringClass) {
            if (type instanceof ParameterizedType) {
                type = ((Class<?>) ((ParameterizedType) type).getRawType()).getGenericSuperclass();
            } else {
                type = ((Class<?>) type).getGenericSuperclass();
            }
        }
        return (Class<T>) ((ParameterizedType) type).getActualTypeArguments()[0];
    }
}
