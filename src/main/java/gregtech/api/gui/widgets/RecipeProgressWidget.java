package gregtech.api.gui.widgets;

import gregtech.api.GTValues;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.recipes.RecipeMap;
import gregtech.integration.jei.GTJeiPlugin;
import gregtech.integration.jei.recipe.RecipeMapCategory;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.util.Collections;
import java.util.function.DoubleSupplier;

public class RecipeProgressWidget extends ProgressWidget {

    private final RecipeMap<?> recipeMap;
    private final static int HOVER_TEXT_WIDTH = 200;

    public RecipeProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height, RecipeMap<?> recipeMap) {
        super(progressSupplier, x, y, width, height);
        this.recipeMap = recipeMap;
    }

    public RecipeProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height, TextureArea fullImage, MoveType moveType, RecipeMap<?> recipeMap) {
        super(progressSupplier, x, y, width, height, fullImage, moveType);
        this.recipeMap = recipeMap;
    }

    public RecipeProgressWidget(int ticksPerCycle, int x, int y, int width, int height, TextureArea fullImage, MoveType moveType, RecipeMap<?> recipeMap) {
        super(ticksPerCycle, x, y, width, height, fullImage, moveType);
        this.recipeMap = recipeMap;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!GTValues.isModLoaded(GTValues.MODID_JEI))
            return false;
        if (isMouseOverElement(mouseX, mouseY) && RecipeMapCategory.getCategoryMap().containsKey(recipeMap)) {
            // Since categories were even registered at all, we know JEI is active.
            GTJeiPlugin.jeiRuntime.getRecipesGui().showCategories(Collections.singletonList(RecipeMapCategory.getCategoryMap().get(recipeMap).getUid()));
            return true;
        }
        return false;
    }


    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        super.drawInForeground(mouseX, mouseY);
        if (isMouseOverElement(mouseX, mouseY) && GTValues.isModLoaded(GTValues.MODID_JEI)) {
            Minecraft mc = Minecraft.getMinecraft();
            GuiUtils.drawHoveringText(Collections.singletonList("Show Recipes"), mouseX, mouseY,
                    sizes.getScreenWidth(),
                    sizes.getScreenHeight(), HOVER_TEXT_WIDTH, mc.fontRenderer);
        }
    }

}
