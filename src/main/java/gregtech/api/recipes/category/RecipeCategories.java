package gregtech.api.recipes.category;

import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;

public final class RecipeCategories {

    public static final GTRecipeCategory ARC_FURNACE_RECYCLING = GTRecipeCategory.create(GTValues.MODID, "arc_furnace_recycling")
            .jeiIcon(GuiTextures.BUTTON_VOID_PARTIAL);

    public static final GTRecipeCategory MACERATOR_RECYCLING = GTRecipeCategory.create(GTValues.MODID, "macerator_recycling")
            .jeiIcon(GuiTextures.BUTTON_VOID_PARTIAL);

    public static final GTRecipeCategory EXTRACTOR_RECYCLING = GTRecipeCategory.create(GTValues.MODID, "extractor_recycling")
            .jeiIcon(GuiTextures.BUTTON_VOID_PARTIAL);

    private RecipeCategories() {}
}
