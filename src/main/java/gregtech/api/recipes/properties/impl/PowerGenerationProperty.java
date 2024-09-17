package gregtech.api.recipes.properties.impl;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.recipes.lookup.property.PowerCapacityProperty;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.recipes.lookup.property.filter.FilterEqualityBehavior;
import gregtech.api.recipes.lookup.property.filter.IPropertyFilter;
import gregtech.api.recipes.lookup.property.filter.LongAVLFilter;
import gregtech.api.recipes.lookup.property.filter.RecipePropertyWithFilter;
import gregtech.api.util.GTUtility;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagLongArray;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;

public class PowerGenerationProperty extends RecipePropertyWithFilter<PowerPropertyData> {

    public static final String KEY = "generation";

    private static PowerGenerationProperty INSTANCE;

    private PowerGenerationProperty() {
        super(KEY, PowerPropertyData.class);
    }

    public static PowerGenerationProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PowerGenerationProperty();
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
                        "gregtech.recipe.eu_inverted",
                        data.getVoltage(), GTValues.VN[GTUtility.getFloorTierByVoltage(data.getVoltage())],
                        data.getAmperage()),
                x, y, 0x111111);
    }

    @Override
    public boolean filterEquals(@Nullable IPropertyFilter<?> other) {
        return other instanceof PowerGenerationProperty;
    }

    @Override
    public int filterHash() {
        return 1;
    }

    @Override
    public @NotNull Filter<PowerPropertyData> getNewFilter() {
        return new PowerGenerationFilter();
    }

    @Override
    public boolean matches(PropertySet properties, PowerPropertyData value) {
        PowerCapacityProperty data = properties.getDefaultable(PowerCapacityProperty.EMPTY);
        return data.voltage() >= value.getVoltage() && data.amperage() >= value.getAmperage();
    }

    private static final class PowerGenerationFilter implements IPropertyFilter.Filter<PowerPropertyData> {

        private final LongAVLFilter voltageFilter = new LongAVLFilter(FilterEqualityBehavior.GREATER_THAN_OR_EQUAL,
                true, false);
        private final LongAVLFilter amperageFilter = new LongAVLFilter(FilterEqualityBehavior.GREATER_THAN_OR_EQUAL,
                true, false);

        @Override
        public void accumulate(short recipeID, @NotNull PowerPropertyData filterInformation) {
            voltageFilter.accumulate(recipeID, filterInformation.getVoltage());
            amperageFilter.accumulate(recipeID, filterInformation.getAmperage());
        }

        @Override
        public void filter(@NotNull BitSet recipeMask, @NotNull PropertySet properties) {
            PowerCapacityProperty data = properties.getDefaultable(PowerCapacityProperty.EMPTY);
            voltageFilter.filter(recipeMask, data.voltage());
            amperageFilter.filter(recipeMask, data.amperage());
        }
    }
}
