package gregtech.api.block;

import gregtech.api.util.LocalizationUtils;
import gregtech.common.creativetab.GTCreativeTabs;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyHelper;
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

import com.google.common.base.Optional;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class VariantBlock<T extends IStringSerializable & Comparable<T>> extends Block {

    protected PropertyIntMap<T> VARIANT;
    protected T[] VALUES;

    public VariantBlock(@NotNull Material materialIn) {
        super(materialIn);
        updateHarvestLevels();
        setCreativeTab(GTCreativeTabs.TAB_GREGTECH);
        setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, 0));
    }

    protected void updateHarvestLevels() {
        if (VALUES.length > 0 && VALUES[0] instanceof IStateHarvestLevel stateHarvestLevel) {
            for (T t : VALUES) {
                IBlockState state = getState(t);
                setHarvestLevel(stateHarvestLevel.getHarvestTool(state),
                        stateHarvestLevel.getHarvestLevel(state), state);
            }
        }
    }

    @Override
    public void getSubBlocks(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> list) {
        for (T variant : VALUES) {
            list.add(getItemVariant(variant));
        }
    }

    public IBlockState getState(T variant) {
        return getDefaultState().withProperty(VARIANT, VARIANT.getIndexOf(variant));
    }

    public T getState(IBlockState blockState) {
        return VARIANT.getValue(blockState.getValue(VARIANT));
    }

    public T getState(ItemStack stack) {
        return getState(getStateFromMeta(stack.getItemDamage()));
    }

    public ItemStack getItemVariant(T variant) {
        return getItemVariant(variant, 1);
    }

    public ItemStack getItemVariant(T variant, int amount) {
        return new ItemStack(this, amount, VARIANT.getIndexOf(variant));
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState() {
        this.VARIANT = new PropertyIntMap<>("variant", computeVariants());
        this.VALUES = VARIANT.getValues();
        return new BlockStateContainer(this, VARIANT);
    }

    @NotNull
    protected Collection<T> computeVariants() {
        Class<T> enumClass = null;
        for (Class<?> innerClazz : getClass().getClasses()) {
            var enums = innerClazz.getEnumConstants();
            if (enums != null && enums[0] instanceof IStringSerializable) {
                // noinspection unchecked
                enumClass = (Class<T>) innerClazz;
                break;
            }
        }
        if (enumClass == null) {
            enumClass = getActualTypeParameter(getClass(), VariantBlock.class);;
        }
        return Arrays.asList(enumClass.getEnumConstants());
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
        return getDefaultState().withProperty(VARIANT, meta % VALUES.length);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT);
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

        while (!(type instanceof ParameterizedType pType) || pType.getRawType() != declaringClass) {
            if (type instanceof ParameterizedType) {
                type = ((Class<?>) ((ParameterizedType) type).getRawType()).getGenericSuperclass();
            } else {
                type = ((Class<?>) type).getGenericSuperclass();
            }
        }
        var arg = pType.getActualTypeArguments()[0];
        if (!(arg instanceof Class<?>)) {
            throw new ClassCastException(String.format("cannot cast %s to a class!", arg));
        }
        return (Class<T>) pType.getActualTypeArguments()[0];
    }

    protected static class PropertyIntMap<O extends Comparable<O> & IStringSerializable>
                                         extends PropertyHelper<Integer> {

        private final Int2ObjectMap<O> intMap;
        private final Object2IntMap<O> reverse;
        private final O[] allowedObjects;

        @SuppressWarnings("unchecked")
        protected PropertyIntMap(String name, Collection<O> values) {
            super(name, Integer.class);
            if (values.isEmpty()) throw new IllegalArgumentException("values are empty!");
            if (values.size() > 16) throw new IllegalArgumentException("values cannot be greater than 16!");

            this.intMap = new Int2ObjectArrayMap<>(values.size());
            this.reverse = new Object2IntArrayMap<>(values.size());

            O first = values.iterator().next();
            this.allowedObjects = (O[]) Array.newInstance(first.getClass(), values.size());

            for (O value : values) {
                int size = this.intMap.size();
                this.allowedObjects[size] = value;
                this.intMap.put(size, value);
                this.reverse.put(value, size);
            }
        }

        @Override
        public @NotNull Collection<Integer> getAllowedValues() {
            return this.intMap.keySet();
        }

        public @NotNull O[] getValues() {
            return this.allowedObjects;
        }

        @Override
        @Deprecated
        public @NotNull String getName(@NotNull Integer value) {
            return getNameByInt(value);
        }

        public @NotNull String getNameByInt(int value) {
            return getValue(value).getName();
        }

        @Override
        public @NotNull Optional<Integer> parseValue(@NotNull String value) {
            for (O object : reverse.keySet()) {
                if (object.getName().equals(value)) {
                    return Optional.of(getIndexOf(object));
                }
            }
            return Optional.absent();
        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + this.allowedObjects.hashCode();
        }

        public int getIndexOf(O value) {
            return this.reverse.getInt(value);
        }

        public O getValue(int index) {
            return this.intMap.get(index);
        }
    }
}
