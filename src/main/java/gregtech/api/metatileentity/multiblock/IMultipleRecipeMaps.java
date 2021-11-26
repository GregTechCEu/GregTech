package gregtech.api.metatileentity.multiblock;

import gregtech.api.recipes.RecipeMap;

public interface IMultipleRecipeMaps {

    /**
     *
     * @return whether the Multiblock has more than one {@link RecipeMap}
     */
    default boolean hasMultipleRecipeMaps() {
        return false;
    }

    /**
     * Used to get all possible RecipeMaps a Multiblock can run
     * @return array of RecipeMaps
     */
    @SuppressWarnings("unused")
    RecipeMap<?>[] getAvailableRecipeMaps();

    /**
     * Used to get the current index of the selected RecipeMap
     * @return index of the current recipe
     */
    @SuppressWarnings("unused")
    int getRecipeMapIndex();


    /**
     * Used to add new RecipeMaps to a given Multiblock,
     * @param recipeMaps to add to the Multiblock
     */
    @SuppressWarnings("unused")
    void addRecipeMaps(RecipeMap<?>[] recipeMaps);

    /**
     * sets the current RecipeMap index to a new one
     * @param index the index to set
     */
    void setRecipeMapIndex(int index);

    /**
     *
     * @return the currently selected RecipeMap
     */
    RecipeMap<?> getCurrentRecipeMap();
}
