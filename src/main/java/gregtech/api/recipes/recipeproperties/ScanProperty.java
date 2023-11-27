package gregtech.api.recipes.recipeproperties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import org.jetbrains.annotations.NotNull;

public class ScanProperty extends RecipeProperty<Boolean> {

    public static final String KEY = "scan";

    private static ScanProperty INSTANCE;

    private ScanProperty() {
        super(KEY, Boolean.class);
    }

    @NotNull
    public static ScanProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ScanProperty();
        }
        return INSTANCE;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.scan_for_research"), x, y, color);
    }
}
