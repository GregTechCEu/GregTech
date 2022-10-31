package gregtech.loaders.recipe.handlers;

import gregtech.loaders.recipe.GTRecipeLoaders;

public class RecipeHandlerList {

    public static void register() {
        MaterialRecipeHandler.register();
        GTRecipeLoaders.ORE_PROCESSING.register(OreRecipeHandler::register);
        PartsRecipeHandler.register();
        GTRecipeLoaders.WIRES.register(WireRecipeHandler::register);
        GTRecipeLoaders.WIRE_COMBINING.register(WireCombiningHandler::register);
        PipeRecipeHandler.register();
        GTRecipeLoaders.TOOLS.register(ToolRecipeHandler::register);
        GTRecipeLoaders.POLARIZING.register(PolarizingRecipeHandler::register);
        GTRecipeLoaders.RECYCLING.register(RecyclingRecipeHandler::register);
    }
}
