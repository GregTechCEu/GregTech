package gregtech.api.recipes.properties.impl;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.properties.RecipeProperty;
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

import java.util.Map;
import java.util.TreeMap;

public final class FusionEUToStartProperty extends RecipeProperty<Long> {

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
}
