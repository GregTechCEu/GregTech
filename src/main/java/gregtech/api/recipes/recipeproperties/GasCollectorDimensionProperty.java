package gregtech.api.recipes.recipeproperties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import java.util.List;

public class GasCollectorDimensionProperty extends RecipeProperty<List> {
    public static final String KEY = "dimension";

    private static GasCollectorDimensionProperty INSTANCE;

    private GasCollectorDimensionProperty() {
        super(KEY, List.class);
    }

    public static GasCollectorDimensionProperty getInstance() {
        if (INSTANCE == null)
            INSTANCE = new GasCollectorDimensionProperty();
        return INSTANCE;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.dimensions",
                value, getDimensionsForRecipe(castValue(value))), x, y, color);
    }

    private String getDimensionsForRecipe(List<Integer> value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.size(); i++) {
            builder.append(value.get(i));
            if (i != value.size() - 1)
                builder.append(", ");
        }
        String str = builder.toString();

        if (str.length() >= 13) {
            str = str.substring(0, 10) + "..";
        }
        return str;
    }

}
