package gtqt.loaders.recipe;
import gtqt.loaders.recipe.handlers.GeneralCircuitHandler;
import gtqt.loaders.recipe.handlers.FakeToolRecipes;
import gtqt.loaders.recipe.handlers.HatchHandlers;
import gtqt.loaders.recipe.handlers.OnceToolHandler;
import gtqt.loaders.recipe.handlers.ProgrammableCircuit;
import gtqt.loaders.recipe.handlers.SprayHandlers;

public class RecipeManager {
    public static void register() {
        HatchHandlers.init();
        ProgrammableCircuit.init();
        SprayHandlers.init();
        FakeToolRecipes.register();
        GeneralCircuitHandler.init();
        OnceToolHandler.register();
    }
}
