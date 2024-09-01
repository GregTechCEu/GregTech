package gregtech.api.recipes.recipeproperties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

public final class ResearchProperty extends RecipeProperty<ResearchPropertyData> {

    public static final String KEY = "research";

    private static ResearchProperty INSTANCE;

    private ResearchProperty() {
        super(KEY, ResearchPropertyData.class);
    }

    @NotNull
    public static ResearchProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ResearchProperty();
        }
        return INSTANCE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInfo(@NotNull Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.research"), x, y, color);
    }
}
