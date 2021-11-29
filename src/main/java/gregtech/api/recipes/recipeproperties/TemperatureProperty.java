package gregtech.api.recipes.recipeproperties;

import gregtech.api.unification.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import org.apache.commons.lang3.Validate;

import java.util.Map;
import java.util.TreeMap;

public class TemperatureProperty extends RecipeProperty<Integer> {
    public static final String KEY = "temperature";

    private static final TreeMap<Integer, Object> registeredCoilTypes = new TreeMap<>((x, y) -> y - x);

    private static TemperatureProperty INSTANCE;

    private TemperatureProperty() {
        super(KEY, Integer.class);
    }

    public static TemperatureProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TemperatureProperty();
        }
        return INSTANCE;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.temperature",
                value, getMinTierForTemperature(castValue(value))), x, y, color);
    }

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
        if (coilMaterial == null) {
            registeredCoilTypes.put(temperature, coilName);
        } else {
            registeredCoilTypes.put(temperature, coilMaterial);
        }
    }

}
