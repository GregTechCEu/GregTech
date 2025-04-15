package gtqt.loaders.recipe;

import gtqt.loaders.recipe.handlers.HatchHandlers;
import gtqt.loaders.recipe.handlers.ProgrammableCircuit;

public class RecipeManager {
    public static void register() {
        HatchHandlers.init();
        ProgrammableCircuit.init();
    }
}
