package gregtech.api.recipes.properties.impl;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;

import gregtech.api.recipes.lookup.property.PowerSupplyProperty;
import gregtech.api.recipes.lookup.property.filter.FilterEqualityBehavior;
import gregtech.api.recipes.lookup.property.filter.IPropertyFilter;
import gregtech.api.recipes.lookup.property.filter.LongAVLFilter;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.recipes.lookup.property.filter.RecipePropertyWithFilter;
import gregtech.api.util.GTUtility;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;

import net.minecraft.nbt.NBTTagLongArray;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;

public final class PowerUsageProperty extends RecipePropertyWithFilter<PowerPropertyData> {

    public static final String KEY = "usage";

    private static PowerUsageProperty INSTANCE;

    private PowerUsageProperty() {
        super(KEY, PowerPropertyData.class);
    }

    public static PowerUsageProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PowerUsageProperty();
            GregTechAPI.RECIPE_PROPERTIES.register(KEY, INSTANCE);
        }
        return INSTANCE;
    }

    @Override
    public @NotNull NBTBase serialize(@NotNull Object value) {
        return new NBTTagLongArray(castValue(value).toLongArray());
    }

    @Override
    public @NotNull Object deserialize(@NotNull NBTBase nbt) {
        return PowerPropertyData.fromLongArray(((NBTTagLongArray) nbt).data);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        PowerPropertyData data = castValue(value);
        minecraft.fontRenderer.drawString(
                I18n.format(
                        "gregtech.recipe.eu",
                        data.getVoltage(), GTValues.VN[GTUtility.getTierByVoltage(data.getVoltage())],
                        data.getAmperage()),
                x, y, 0x111111);
    }

    @Override
    public boolean filterEquals(@Nullable IPropertyFilter<?> other) {
        return other instanceof PowerUsageProperty;
    }

    @Override
    public int filterHash() {
        return 0;
    }

    @Override
    public @NotNull PowerUsageFilter getNewFilter() {
        return new PowerUsageFilter();
    }

    private static final class PowerUsageFilter implements IPropertyFilter.Filter<PowerPropertyData> {

        private final LongAVLFilter voltageFilter = new LongAVLFilter(FilterEqualityBehavior.GREATER_THAN_OR_EQUAL, true, false);
        private final LongAVLFilter amperageFilter = new LongAVLFilter(FilterEqualityBehavior.GREATER_THAN_OR_EQUAL, true, false);

        @Override
        public void accumulate(short recipeID, @NotNull PowerPropertyData filterInformation) {
            voltageFilter.accumulate(recipeID, filterInformation.getVoltage());
            amperageFilter.accumulate(recipeID, filterInformation.getAmperage());
        }

        @Override
        public void filter(@NotNull BitSet recipeMask, @NotNull PropertySet properties) {
            PowerSupplyProperty data = properties.getDefaultable(PowerSupplyProperty.EMPTY);
            voltageFilter.filter(recipeMask, data.voltage());
            amperageFilter.filter(recipeMask, data.amperage());
        }
    }
}
