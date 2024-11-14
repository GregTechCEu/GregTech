package gregtech.api.recipes.properties.impl;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.recipes.lookup.property.TemperatureMaximumProperty;
import gregtech.api.recipes.lookup.property.filter.FilterEqualityBehavior;
import gregtech.api.recipes.lookup.property.filter.IPropertyFilter;
import gregtech.api.recipes.lookup.property.filter.IntAVLFilter;
import gregtech.api.recipes.lookup.property.filter.RecipePropertyWithFilter;
import gregtech.api.recipes.properties.RecipeProperty;
import gregtech.api.unification.material.Material;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;

public final class TemperatureProperty extends RecipePropertyWithFilter<Integer> {

    public static final String KEY = "temperature";

    private static final Int2ObjectAVLTreeMap<Object> registeredCoilTypes = new Int2ObjectAVLTreeMap<>((x, y) -> y - x);

    private static TemperatureProperty INSTANCE;

    private TemperatureProperty() {
        super(KEY, Integer.class);
    }

    public static TemperatureProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TemperatureProperty();
            GregTechAPI.RECIPE_PROPERTIES.register(KEY, INSTANCE);
        }
        return INSTANCE;
    }

    @Override
    public @NotNull NBTBase serialize(@NotNull Object value) {
        return new NBTTagInt(castValue(value));
    }

    @Override
    public @NotNull Object deserialize(@NotNull NBTBase nbt) {
        return ((NBTTagInt) nbt).getInt();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.temperature",
                value, getMinTierForTemperature(castValue(value))), x, y, color);
    }

    @NotNull
    private String getMinTierForTemperature(Integer value) {
        String name = "";
        for (var coil : registeredCoilTypes.int2ObjectEntrySet()) {
            if (value <= coil.getIntKey()) {
                Object mapValue = coil.getValue();
                if (mapValue instanceof Material) {
                    name = ((Material) mapValue).getLocalizedName();
                } else if (mapValue instanceof String) {
                    name = I18n.format((String) mapValue);
                }
            }
        }
        if (name.length() >= 13) {
            name = name.substring(0, 10) + "..";
        }
        return name;
    }

    /**
     * This Maps coil Materials to its Integer temperatures.
     * In case the coil was not constructed with a Material you can pass a String name,
     * ideally an unlocalized name
     */
    public static void registerCoilType(int temperature, Material coilMaterial, String coilName) {
        Validate.notNull(coilName);
        if (coilMaterial == null) {
            registeredCoilTypes.put(temperature, coilName);
        } else {
            registeredCoilTypes.put(temperature, coilMaterial);
        }
    }

    @Override
    public boolean filterEquals(@Nullable IPropertyFilter<?> other) {
        return other instanceof TemperatureProperty;
    }

    @Override
    public int filterHash() {
        return 4;
    }

    @Override
    public @NotNull Filter<Integer> getNewFilter() {
        return new TemperatureFilter();
    }

    @Override
    public boolean matches(PropertySet properties, Integer value) {
        return properties.getDefaultable(TemperatureMaximumProperty.EMPTY).temperature() >= value;
    }

    private static final class TemperatureFilter implements IPropertyFilter.Filter<Integer> {

        private final IntAVLFilter filter = new IntAVLFilter(FilterEqualityBehavior.GREATER_THAN_OR_EQUAL, true, false);

        @Override
        public void accumulate(short recipeID, @NotNull Integer filterInformation) {
            filter.accumulate(recipeID, filterInformation);
        }

        @Override
        public void filter(@NotNull BitSet recipeMask, @NotNull PropertySet properties) {
            filter.filter(recipeMask, properties.getDefaultable(TemperatureMaximumProperty.EMPTY).temperature());
        }
    }
}
