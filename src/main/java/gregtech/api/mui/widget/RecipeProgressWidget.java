package gregtech.api.mui.widget;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.widgets.ProgressWidget;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.RecipeMap;

import gregtech.api.recipes.RecipeMaps;
import gregtech.integration.IntegrationModule;
import gregtech.integration.jei.JustEnoughItemsModule;
import gregtech.integration.jei.recipe.RecipeMapCategory;
import gregtech.modules.GregTechModules;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RecipeProgressWidget extends ProgressWidget implements Interactable {

    private RecipeMap<?> recipeMap;

    public RecipeProgressWidget recipeMap(RecipeMap<?> recipeMap) {
        this.recipeMap = recipeMap;
        if (GregTechAPI.moduleManager.isModuleEnabled(GregTechModules.MODULE_JEI)) {
            tooltip(t -> t.addLine(IKey.lang("gui.widget.recipeProgressWidget.default_tooltip")));
        }
        return this;
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        return Interactable.super.onMousePressed(mouseButton);
        // TODO Waiting for a MUI solution to do this.
        // TODO Crashes when exiting the MUI ui after closing the JEI ui
        // TODO that was opened by this widget.
        /*
        if (recipeMap == null) {
            return Result.IGNORE;
        }
        if (mouseButton == 0 || mouseButton == 1) {
            if (!GregTechAPI.moduleManager.isModuleEnabled(GregTechModules.MODULE_JEI)) {
                return Result.ACCEPT;
            }

            Collection<RecipeMapCategory> categories = RecipeMapCategory.getCategoriesFor(recipeMap);
            if (categories != null && !categories.isEmpty()) {
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
                    return Result.ACCEPT;
                }
                JustEnoughItemsModule.jeiRuntime.getRecipesGui().showCategories(categoryID);
                return Result.SUCCESS;
            }
        }
        return Result.IGNORE;*/
    }
}
