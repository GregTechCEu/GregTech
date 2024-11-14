package gregtech.api.recipes.properties.impl;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.lookup.property.MaxCWUtProperty;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.recipes.lookup.property.filter.FilterEqualityBehavior;
import gregtech.api.recipes.lookup.property.filter.IPropertyFilter;
import gregtech.api.recipes.lookup.property.filter.IntAVLFilter;
import gregtech.api.recipes.lookup.property.filter.RecipePropertyWithFilter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;

public final class ComputationProperty extends RecipePropertyWithFilter<Integer> {

    public static final String KEY = "computation_per_tick";

    private static ComputationProperty INSTANCE;

    private ComputationProperty() {
        super(KEY, Integer.class);
    }

    public static ComputationProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ComputationProperty();
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
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.computation_per_tick", castValue(value)), x, y,
                color);
    }

    @Override
    public boolean filterEquals(@Nullable IPropertyFilter<?> other) {
        return other instanceof ComputationProperty;
    }

    @Override
    public int filterHash() {
        return 6;
    }

    @Override
    public @NotNull Filter<Integer> getNewFilter() {
        return new ComputationFilter();
    }

    @Override
    public boolean matches(PropertySet properties, Integer value) {
        MaxCWUtProperty property = properties.getDefaultable(MaxCWUtProperty.EMPTY);
        return property.CWUt() >= value;
    }

    private static final class ComputationFilter implements Filter<Integer> {

        private final IntAVLFilter filter = new IntAVLFilter(FilterEqualityBehavior.GREATER_THAN_OR_EQUAL, true, false);

        @Override
        public void accumulate(short recipeID, @NotNull Integer filterInformation) {
            filter.accumulate(recipeID, filterInformation);
        }

        @Override
        public void filter(@NotNull BitSet recipeMask, @NotNull PropertySet properties) {
            MaxCWUtProperty property = properties.getDefaultable(MaxCWUtProperty.EMPTY);
            filter.filter(recipeMask, property.CWUt());
        }
    }
}
