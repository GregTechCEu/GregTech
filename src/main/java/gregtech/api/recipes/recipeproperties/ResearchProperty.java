package gregtech.api.recipes.recipeproperties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;

public final class ResearchProperty extends RecipeProperty<ResearchPropertyData> {

    public static final String KEY = "research";

    private static ResearchProperty INSTANCE;

    private ResearchProperty() {
        super(KEY, ResearchPropertyData.class);
    }

    @Nonnull
    public static ResearchProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ResearchProperty();
        }
        return INSTANCE;
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.research"), x, y, color);
    }
}
