package gregtech.loaders.recipe.handlers;

import gregtech.common.ConfigHolder;

public class RecipeHandlerList {

    public static void register() {
        MaterialRecipeHandler.register();
        OreRecipeHandler.register();
        PartsRecipeHandler.register();
        WireRecipeHandler.register();
        if (!ConfigHolder.recipeRemovalConfig.processingHandlers.disableWireRecipes) WireCombiningHandler.register();
        PipeRecipeHandler.register();
        ToolRecipeHandler.register();
        PolarizingRecipeHandler.register();
        RecyclingRecipeHandler.register();
    }
}
