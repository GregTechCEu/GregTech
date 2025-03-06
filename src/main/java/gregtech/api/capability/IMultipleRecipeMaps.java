package gregtech.api.capability;

import gregtech.api.recipes.RecipeMap;

import org.jetbrains.annotations.Nullable;

public interface IMultipleRecipeMaps extends IHasRecipeMap {

    /**
     * Used to get all possible RecipeMaps a Multiblock can run
     * 
     * @return array of RecipeMaps
     */
    RecipeMap<?>[] getAvailableRecipeMaps();

    /**
     *
     * @return the currently selected RecipeMap
     */
    RecipeMap<?> getCurrentRecipeMap();

    @Override
    default @Nullable RecipeMap<?> getRecipeMap() {
        return getCurrentRecipeMap();
    }

    /** @return the index of the currently selected RecipeMap. Used for UI. */
    int getRecipeMapIndex();

    /** Set the current RecipeMap by index. Used for UI. */
    void setRecipeMapIndex(int index);
}
