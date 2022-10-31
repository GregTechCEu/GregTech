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
        GTRecipeLoaders.POWER_DEVICES.register(MetaTileEntityMachineRecipeLoader::init);
        RecipeHandlerList.register();
    }

    public static void loadLatest() {
        GTRecipeLoaders.DECOMPOSITION.register(DecompositionRecipeHandler::runRecipeGeneration);
        GTRecipeLoaders.RECYCLING.register(RecyclingRecipes::init);
        GTRecipeLoaders.WOOD_PROCESSING.register(WoodMachineRecipes::init);
    }

    public static void postLoad() {
        GTRecipeLoaders.WOOD_PROCESSING.register(WoodMachineRecipes::postInit);
    }
}
