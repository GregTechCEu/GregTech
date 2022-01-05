package gregtech.loaders.recipe;

import gregtech.loaders.recipe.handlers.DecompositionRecipeHandler;
import gregtech.loaders.recipe.handlers.RecipeHandlerList;
import gregtech.loaders.recipe.handlers.ToolRecipeHandler;

public class GTRecipeManager {

    public static void preLoad() {
        ToolRecipeHandler.initializeMetaItems();
    }

    public static void load() {
        MachineRecipeLoader.init();
        CraftingRecipeLoader.init();
        MetaTileEntityLoader.init();
        MetaTileEntityMachineRecipeLoader.init();
        RecipeHandlerList.register();
    }

    public static void loadLatest() {
        DecompositionRecipeHandler.runRecipeGeneration();
        RecyclingRecipes.init();
        WoodMachineRecipes.init();
    }

    public static void postLoad() {
        WoodMachineRecipes.postInit();
    }
}
