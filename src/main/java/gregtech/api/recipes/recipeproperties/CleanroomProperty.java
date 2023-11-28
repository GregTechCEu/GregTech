package gregtech.api.recipes.recipeproperties;

import gregtech.api.metatileentity.multiblock.CleanroomType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import org.jetbrains.annotations.NotNull;

public class CleanroomProperty extends RecipeProperty<CleanroomType> {

    public static final String KEY = "cleanroom";

    private static CleanroomProperty INSTANCE;

    private CleanroomProperty() {
        super(KEY, CleanroomType.class);
    }

    public static CleanroomProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CleanroomProperty();
        }
        return INSTANCE;
    }

    @Override
    public void drawInfo(@NotNull Minecraft minecraft, int x, int y, int color, Object value) {
        CleanroomType type = castValue(value);
        if (type == null) return;

        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.cleanroom", getName(type)), x, y, color);
    }

    @NotNull
    private static String getName(@NotNull CleanroomType value) {
        String name = I18n.format(value.getTranslationKey());
        if (name.length() >= 20) return name.substring(0, 20) + "..";
        return name;
    }
}
