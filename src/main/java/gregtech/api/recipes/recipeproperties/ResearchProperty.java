package gregtech.api.recipes.recipeproperties;

import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;

public class ResearchProperty extends RecipeProperty<String> {

    public static final String KEY = "research";

    private static ResearchProperty INSTANCE;

    private ResearchProperty() {
        super(KEY, String.class);
    }

    public static ResearchProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ResearchProperty();
        }
        return INSTANCE;
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int x, int y, int color, Object value) {/**/}
}
