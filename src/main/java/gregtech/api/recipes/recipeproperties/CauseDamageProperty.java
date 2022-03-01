package gregtech.api.recipes.recipeproperties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class CauseDamageProperty extends RecipeProperty<Float> {

    public static final String KEY = "cause_damage";
    private static CauseDamageProperty INSTANCE;

    public static CauseDamageProperty getInstance() {
        if (INSTANCE == null)
            INSTANCE = new CauseDamageProperty();
        return INSTANCE;
    }

    private CauseDamageProperty() {
        super(KEY, Float.class);
    }

    protected CauseDamageProperty(String key, Class<Float> type) {
        super(key, type);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.cause_damage",
                castValue(value)), x, y, color);
    }
}
