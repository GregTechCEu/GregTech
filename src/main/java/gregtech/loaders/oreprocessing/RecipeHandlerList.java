package gregtech.loaders.oreprocessing;

public class RecipeHandlerList {

    public static void register() {
        MaterialRecipeHandler.register();
        OreRecipeHandler.register();
        PartsRecipeHandler.register();
        WireRecipeHandler.register();
        WireCombiningHandler.register();
        PipeRecipeHandler.register();
        ToolRecipeHandler.register();
        PolarizingRecipeHandler.register();
        RecyclingRecipeHandler.register();
    }
}
