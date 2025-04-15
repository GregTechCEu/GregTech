package gregtech.loaders.recipe.handlers;

public class RecipeHandlerList {

    public static void register() {
        MaterialRecipeHandler.register();
        OreRecipeHandler.register();
        PartsRecipeHandler.register();
        PartRecipeHandler1.register();
        WireRecipeHandler.register();
        WireCombiningHandler.register();
        PipeRecipeHandler.register();
        ToolRecipeHandler.register();
        PolarizingRecipeHandler.register();
        RecyclingRecipeHandler.register();
    }
}
