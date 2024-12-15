package gregtech.api.recipes.ui;

import gregtech.api.recipes.RecipeMap;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface RecipeMapUIFunction {

    /**
     * Create a RecipeMapUI using a RecipeMap.
     *
     * @param recipeMap the recipemap to associate with the ui
     * @return the ui
     */
    @NotNull
    RecipeMapUI<?> apply(@NotNull RecipeMap<?> recipeMap);
}
