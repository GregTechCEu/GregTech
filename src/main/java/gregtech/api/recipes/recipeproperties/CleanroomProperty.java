package gregtech.api.recipes.recipeproperties;

import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.util.GTUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;

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
    public void drawInfo(@Nonnull Minecraft minecraft, int x, int y, int color, Object value) {
        if (castValue(value) == null)
            return;

        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.cleanroom",
                getName(castValue(value))), x, y, color);
    }

    @Nonnull
    private String getName(@Nonnull CleanroomType value) {
        String name = value.getName();

        if (name.length() >= 13)
            return  GTUtility.lowerUnderscoreToUpperCamel(name.substring(0, 10)) + "..";

        return GTUtility.lowerUnderscoreToUpperCamel(name);
    }
}
