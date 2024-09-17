package gregtech.api.recipes.lookup;

import gregtech.api.recipes.Recipe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

public abstract class IndexedRecipeLookup extends AbstractRecipeLookup {

    /**
     * @throws IndexOutOfBoundsException if {@code index} equals or exceeds {@link #getRecipeCount()}
     * @param index the index to get the recipe for.
     * @return the recipe at the index.
     */
    public @NotNull Recipe getRecipeByIndex(int index) {
        return getAllRecipes().get(index);
    }

    @Override
    public abstract @NotNull @UnmodifiableView List<Recipe> getAllRecipes();
}
