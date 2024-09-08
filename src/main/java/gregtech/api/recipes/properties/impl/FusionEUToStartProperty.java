package gregtech.api.recipes.properties.impl;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.lookup.property.EUToStartProperty;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.recipes.lookup.property.filter.FilterEqualityBehavior;
import gregtech.api.recipes.lookup.property.filter.IPropertyFilter;
import gregtech.api.recipes.lookup.property.filter.LongAVLFilter;
import gregtech.api.recipes.lookup.property.filter.RecipePropertyWithFilter;
import gregtech.api.util.TextFormattingUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagLong;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.Map;
import java.util.TreeMap;

public final class FusionEUToStartProperty extends RecipePropertyWithFilter<Long> {

    public static final String KEY = "eu_to_start";

    private static final TreeMap<Long, Pair<Integer, String>> registeredFusionTiers = new TreeMap<>();

    private static FusionEUToStartProperty INSTANCE;

    private FusionEUToStartProperty() {
        super(KEY, Long.class);
    }

    public static FusionEUToStartProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FusionEUToStartProperty();
            GregTechAPI.RECIPE_PROPERTIES.register(KEY, INSTANCE);
        }

        return INSTANCE;
    }

    @Override
    public @NotNull NBTBase serialize(@NotNull Object value) {
        return new NBTTagLong(castValue(value));
    }

    @Override
    public @NotNull Object deserialize(@NotNull NBTBase nbt) {
        return ((NBTTagLong) nbt).getLong();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.eu_to_start",
                TextFormattingUtil.formatLongToCompactString(castValue(value))) + getFusionTierName(castValue(value)),
                x, y,
                color);
    }

    private static String getFusionTierName(Long eu) {
        Map.Entry<Long, Pair<Integer, String>> mapEntry = registeredFusionTiers.ceilingEntry(eu);

        if (mapEntry == null) {
            throw new IllegalArgumentException("Value is above registered maximum EU values");
        }

        return String.format(" %s", mapEntry.getValue().getRight());
    }

    public static int getFusionTier(Long eu) {
        Map.Entry<Long, Pair<Integer, String>> mapEntry = registeredFusionTiers.ceilingEntry(eu);
        return mapEntry == null ? 0 : mapEntry.getValue().getLeft();
    }

    public static void registerFusionTier(int tier, String shortName) {
        Validate.notNull(shortName);
        long maxEU = 16 * 10000000L * (long) Math.pow(2, tier - 6);
        registeredFusionTiers.put(maxEU, Pair.of(tier, shortName));
    }

    @Override
    public boolean filterEquals(@Nullable IPropertyFilter<?> other) {
        return other instanceof FusionEUToStartProperty;
    }

    @Override
    public int filterHash() {
        return 3;
    }

    @Override
    public @NotNull Filter<Long> getNewFilter() {
        return new EUToStartFilter();
    }

    private static final class EUToStartFilter implements IPropertyFilter.Filter<Long> {

        private final LongAVLFilter filter = new LongAVLFilter(FilterEqualityBehavior.GREATER_THAN_OR_EQUAL, true, false);

        @Override
        public void accumulate(short recipeID, @NotNull Long filterInformation) {
            filter.accumulate(recipeID, filterInformation);
        }

        @Override
        public void filter(@NotNull BitSet recipeMask, @NotNull PropertySet properties) {
            filter.filter(recipeMask, properties.getDefaultable(EUToStartProperty.EMPTY).eu());
        }
    }
}
