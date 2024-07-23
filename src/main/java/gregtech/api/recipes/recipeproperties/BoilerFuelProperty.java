package gregtech.api.recipes.recipeproperties;

import gregtech.api.recipes.builders.FusionRecipeBuilder;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;
import gregtech.common.ConfigHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class BoilerFuelProperty extends RecipeProperty<Float> {
    public static final String KEY = "boiler_fuel";
    private static BoilerFuelProperty INSTANCE;
    private BoilerFuelProperty() {super( KEY, Float.class ); }

    public static BoilerFuelProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BoilerFuelProperty();
        }
        return INSTANCE;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {}

    @Override
    public boolean hideTotalEU() {
        return true;
    }

    @Override
    public boolean hideEUt() {
        return true;
    }
}
