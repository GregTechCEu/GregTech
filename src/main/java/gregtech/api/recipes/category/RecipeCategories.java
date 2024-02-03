package gregtech.api.recipes.category;

import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.recipes.RecipeMaps;

public final class RecipeCategories {

    public static final GTRecipeCategory SMELT_RECYCLING = GTRecipeCategory.create(GTValues.MODID,
            "arc_furnace_recycling",
            "gregtech.recipe.category.arc_furnace_recycling",
            RecipeMaps.BLAST_RECIPES)
            .jeiIcon(GuiTextures.ARC_FURNACE_RECYLCING_CATEGORY);

    public static final GTRecipeCategory MACERATOR_RECYCLING = GTRecipeCategory.create(GTValues.MODID,
                    "macerator_recycling",
                    "gregtech.recipe.category.macerator_recycling",
                    RecipeMaps.MACERATOR_RECIPES)
            .jeiIcon(GuiTextures.MACERATOR_RECYLCING_CATEGORY);

    private RecipeCategories() {}
}
