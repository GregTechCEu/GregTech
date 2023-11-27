package gregtech.api.recipes.recipeproperties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class DefaultProperty<T> extends RecipeProperty<T> {

    public DefaultProperty(String key, Class<T> type) {
        super(key, type);
    }

    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe." + getKey(),
                castValue(value)), x, y, color);
    }
}
