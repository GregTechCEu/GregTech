package gregtech.api.recipes.recipeproperties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;

public class RadiationProperty extends RecipeProperty<Float> {
    public static final String KEY = "radiation";

    private static RadiationProperty INSTANCE;

    private RadiationProperty() {
        super(KEY, Float.class);
    }

    public static RadiationProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RadiationProperty();
        }
        return INSTANCE;
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int x, int y, int color, Object value) {
        Float type = castValue(value);
        if (type == null) return;

        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.radiation", type), x, y, color);
    }
}
