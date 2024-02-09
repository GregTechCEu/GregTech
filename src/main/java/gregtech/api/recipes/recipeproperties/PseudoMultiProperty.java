package gregtech.api.recipes.recipeproperties;

import gregtech.api.recipes.recipeproperties.RecipeProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import org.jetbrains.annotations.NotNull;

public class PseudoMultiProperty extends RecipeProperty<PseudoMultiPropertyValues> {
    public static final String KEY = "blocks";

    private static PseudoMultiProperty INSTANCE;
    private PseudoMultiProperty() {
        super(KEY, PseudoMultiPropertyValues.class);
    }

    public static PseudoMultiProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PseudoMultiProperty();
        }
        return INSTANCE;
    }

    @Override
    public void drawInfo(@NotNull Minecraft minecraft, int x, int y, int color, Object value) {
        PseudoMultiPropertyValues propertyValue = castValue(value);
        String localisedBlockGroupMembers = I18n.format("gregtech.block_group_members." + propertyValue.getBlockGroupName() + ".name");
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.blocks", localisedBlockGroupMembers), x, y, color);
    }

}
