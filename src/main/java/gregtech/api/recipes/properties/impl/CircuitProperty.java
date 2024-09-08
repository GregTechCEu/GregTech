package gregtech.api.recipes.properties.impl;

import gregtech.api.GregTechAPI;

import gregtech.api.recipes.lookup.property.CircuitPresenceProperty;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.recipes.lookup.property.filter.IPropertyFilter;
import gregtech.api.recipes.lookup.property.filter.RecipePropertyWithFilter;

import gregtech.common.items.MetaItems;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;

import net.minecraft.nbt.NBTTagByte;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;

public final class CircuitProperty extends RecipePropertyWithFilter<Byte> {

    public static final byte CIRCUIT_MAX = 32;
    public static final byte CIRCUIT_MIN = 0;

    public static final String KEY = "circuit";

    private static CircuitProperty INSTANCE;

    private CircuitProperty() {
        super(KEY, Byte.class);
    }

    public static CircuitProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CircuitProperty();
            GregTechAPI.RECIPE_PROPERTIES.register(KEY, INSTANCE);
        }
        return INSTANCE;
    }

    @Override
    public @NotNull NBTBase serialize(@NotNull Object value) {
        return new NBTTagByte(castValue(value));
    }

    @Override
    public @NotNull Object deserialize(@NotNull NBTBase nbt) {
        return ((NBTTagByte) nbt).getByte();
    }

    @Override
    public int getInfoHeight(@NotNull Object value) {
        return 18;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        ItemStack circuitStack = getIntegratedCircuit(castValue(value));
        minecraft.getItemRenderer().itemRenderer.renderItemIntoGUI(circuitStack, x, y);
    }

    public static ItemStack getIntegratedCircuit(int configuration) {
        ItemStack stack = MetaItems.INTEGRATED_CIRCUIT.getStackForm();
        setCircuitConfiguration(stack, configuration);
        return stack;
    }

    public static void setCircuitConfiguration(ItemStack itemStack, int configuration) {
        if (!MetaItems.INTEGRATED_CIRCUIT.isItemEqual(itemStack))
            throw new IllegalArgumentException("Given item stack is not an integrated circuit!");
        if (configuration < 0 || configuration > CIRCUIT_MAX)
            throw new IllegalArgumentException("Given configuration number is out of range!");
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            itemStack.setTagCompound(tagCompound);
        }
        tagCompound.setInteger("Configuration", configuration);
    }

    public static int getCircuitConfiguration(ItemStack itemStack) {
        if (!isIntegratedCircuit(itemStack)) return 0;
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            return tagCompound.getInteger("Configuration");
        }
        return 0;
    }

    public static boolean isIntegratedCircuit(ItemStack itemStack) {
        boolean isCircuit = MetaItems.INTEGRATED_CIRCUIT.isItemEqual(itemStack);
        if (isCircuit && !itemStack.hasTagCompound()) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger("Configuration", 0);
            itemStack.setTagCompound(compound);
        }
        return isCircuit;
    }

    @Override
    public boolean filterEquals(@Nullable IPropertyFilter<?> other) {
        return other instanceof CircuitProperty;
    }

    @Override
    public int filterHash() {
        return -1;
    }

    @Override
    public @NotNull Filter<Byte> getNewFilter() {
        return new CircuitFilterMap();
    }

    private static final class CircuitFilterMap extends Byte2ObjectArrayMap<BitSet> implements Filter<Byte> {

        @Override
        public void accumulate(short recipeID, @NotNull Byte filterInformation) {
            this.computeIfAbsent(filterInformation, k -> new BitSet()).set(recipeID);
        }

        @Override
        public void filter(@NotNull BitSet recipeMask, @NotNull PropertySet properties) {
            for (var entry : this.byte2ObjectEntrySet()) {
                if (!properties.contains(CircuitPresenceProperty.get(entry.getByteKey()))) {
                    recipeMask.or(entry.getValue());
                }
            }
        }
    }
}
