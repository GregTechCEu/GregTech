package gregtech.api.gui.widgets;

import gregtech.api.GTValues;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.integration.jei.GTJeiPlugin;
import gregtech.integration.jei.recipe.RecipeMapCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
        if (!Loader.isModLoaded(GTValues.MODID_JEI))
            return false;
        if (isMouseOverElement(mouseX, mouseY) && RecipeMapCategory.getCategoryMap().containsKey(recipeMap)) {
            // Since categories were even registered at all, we know JEI is active.
            List<String> categoryID = new ArrayList<>();
            if(recipeMap == RecipeMaps.FURNACE_RECIPES) {
                categoryID.add("minecraft.smelting");
            }
            else {
                categoryID.add(RecipeMapCategory.getCategoryMap().get(recipeMap).getUid());
            }
            GTJeiPlugin.jeiRuntime.getRecipesGui().showCategories(categoryID);
            return true;
        }
        return false;
    }


    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        super.drawInForeground(mouseX, mouseY);
        if (isMouseOverElement(mouseX, mouseY) && Loader.isModLoaded(GTValues.MODID_JEI)) {
            Minecraft mc = Minecraft.getMinecraft();
            GuiUtils.drawHoveringText(Collections.singletonList(I18n.format("gui.widget.recipeProgressWidget.default_tooltip")), mouseX, mouseY,
                    sizes.getScreenWidth(),
                    sizes.getScreenHeight(), HOVER_TEXT_WIDTH, mc.fontRenderer);
        }
    }

}
