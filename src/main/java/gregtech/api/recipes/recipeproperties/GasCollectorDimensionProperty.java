package gregtech.api.recipes.recipeproperties;

import gregtech.api.worldgen.config.WorldGenRegistry;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import java.util.List;
import java.util.Map;

public class GasCollectorDimensionProperty extends RecipeProperty<IntList> {
    public static final String KEY = "dimension";

    private static GasCollectorDimensionProperty INSTANCE;

    private GasCollectorDimensionProperty() {
        super(KEY, IntList.class);
    }

    public static GasCollectorDimensionProperty getInstance() {
        if (INSTANCE == null)
            INSTANCE = new GasCollectorDimensionProperty();
        return INSTANCE;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.dimensions",
                getDimensionsForRecipe(castValue(value))), x, y, color);
    }

    private String getDimensionsForRecipe(List<Integer> value) {
        Map<Integer, String> dimNames = WorldGenRegistry.getNamedDimensions();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.size(); i++) {
            builder.append(dimNames.getOrDefault(value.get(i), "" + value.get(i)));
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
