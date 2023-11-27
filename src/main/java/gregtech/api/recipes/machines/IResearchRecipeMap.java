package gregtech.api.recipes.machines;

import gregtech.api.recipes.Recipe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface IResearchRecipeMap {

    /**
     * Add a recipe to the data stick registry for the {@link gregtech.api.recipes.RecipeMap}
     *
     * @param researchId the ID to match recipes to, typically derived from the recipe output
     * @param recipe     the recipe to add to the registry
     */
    void addDataStickEntry(@NotNull String researchId, @NotNull Recipe recipe);

    /**
     * @param researchId the ID to match recipes to, typically derived from the recipe output
     * @return the set of recipes assigned to the ID
     */
    @Nullable
    Collection<Recipe> getDataStickEntry(@NotNull String researchId);

    /**
     * Remove a recipe from the data stick registry for the {@link gregtech.api.recipes.RecipeMap}
     *
     * @param researchId the ID to match recipes to, typically derived from the recipe output
     * @param recipe     the recipe to remove from the registry
     * @return true if the recipe was successfully removed, otherwise false
     */
    boolean removeDataStickEntry(@NotNull String researchId, @NotNull Recipe recipe);
}
