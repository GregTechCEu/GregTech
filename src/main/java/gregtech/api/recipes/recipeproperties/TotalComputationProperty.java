package gregtech.api.recipes.recipeproperties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class TotalComputationProperty extends RecipeProperty<Integer> {

    public static final String KEY = "total_computation";

    private static TotalComputationProperty INSTANCE;

    protected TotalComputationProperty() {
        super(KEY, Integer.class);
    }

    public static TotalComputationProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TotalComputationProperty();
        }
        return INSTANCE;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.total_computation", castValue(value)), x, y,
                color);
    }

    @Override
    public boolean hideDuration() {
        return true;
    }
}
