package gtqt.loaders.recipe;

import gtqt.loaders.recipe.handlers.HatchHandlers;

public class RecipeManager {
    public static void register() {
        HatchHandlers.init();
    }
}
