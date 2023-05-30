package gregtech.api.recipes.category;

import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.recipes.RecipeMaps;

public final class RecipeCategories {

    public static final GTRecipeCategory ARC_FURNACE_RECYCLING = GTRecipeCategory.create(GTValues.MODID, "arc_furnace_recycling", RecipeMaps.ARC_FURNACE_RECIPES)
            .jeiIcon(GuiTextures.BUTTON_VOID_PARTIAL);

    public static final GTRecipeCategory MACERATOR_RECYCLING = GTRecipeCategory.create(GTValues.MODID, "macerator_recycling", RecipeMaps.MACERATOR_RECIPES)
            .jeiIcon(GuiTextures.BUTTON_VOID_PARTIAL);

    public static final GTRecipeCategory EXTRACTOR_RECYCLING = GTRecipeCategory.create(GTValues.MODID, "extractor_recycling", RecipeMaps.EXTRACTOR_RECIPES)
            .jeiIcon(GuiTextures.BUTTON_VOID_PARTIAL);

    private RecipeCategories() {}
}
