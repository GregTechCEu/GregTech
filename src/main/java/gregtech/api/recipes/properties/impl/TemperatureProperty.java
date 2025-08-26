package gregtech.api.recipes.properties.impl;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.properties.RecipeProperty;
import gregtech.api.unification.material.Material;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.TreeMap;

public final class TemperatureProperty extends RecipeProperty<Integer> {

    public static final String KEY = "temperature";

    private static final TreeMap<Integer, Object> registeredCoilTypes = new TreeMap<>((x, y) -> y - x);

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
        for (Map.Entry<Integer, Object> coil : registeredCoilTypes.entrySet()) {
            if (value <= coil.getKey()) {
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
        registeredCoilTypes.put(temperature, coilMaterial == null ? coilName : coilMaterial);
    }
}
