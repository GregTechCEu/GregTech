package gregtech.api.recipes.recipeproperties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class ComputationProperty extends RecipeProperty<Integer> {

    public static final String KEY = "computation_per_tick";

    private static ComputationProperty INSTANCE;

    protected ComputationProperty() {
        super(KEY, Integer.class);
    }

    public static ComputationProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ComputationProperty();
        }
        return INSTANCE;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.computation_per_tick", castValue(value)), x, y,
                color);
    }
}
