package gregtech.api.gui.widgets;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.integration.IntegrationModule;
import gregtech.integration.jei.JustEnoughItemsModule;
import gregtech.integration.jei.recipe.RecipeMapCategory;
import gregtech.modules.GregTechModules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Loader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleSupplier;

public class RecipeProgressWidget extends ProgressWidget {

    private final RecipeMap<?> recipeMap;
    private static final int HOVER_TEXT_WIDTH = 200;

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
        if (!GregTechAPI.moduleManager.isModuleEnabled(GregTechModules.MODULE_JEI)) {
            return false;
        }
        if (isMouseOverElement(mouseX, mouseY)) {
            Collection<RecipeMapCategory> categories = RecipeMapCategory.getCategoriesFor(recipeMap);
            if (categories != null && !categories.isEmpty()) {
                // Since categories were even registered at all, we know JEI is active.
                List<String> categoryID = new ArrayList<>();
                if (recipeMap == RecipeMaps.FURNACE_RECIPES) {
                    categoryID.add("minecraft.smelting");
                } else {
                    for (RecipeMapCategory category : categories) {
                        categoryID.add(category.getUid());
                    }
                }

                if (JustEnoughItemsModule.jeiRuntime == null) {
                    IntegrationModule.logger.error("GTCEu JEI integration has crashed, this is not a good thing");
                    return false;
                }
                JustEnoughItemsModule.jeiRuntime.getRecipesGui().showCategories(categoryID);
                return true;
            }
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
