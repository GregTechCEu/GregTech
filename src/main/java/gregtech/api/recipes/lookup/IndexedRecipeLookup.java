package gregtech.api.recipes.lookup;

import gregtech.api.recipes.Recipe;

import org.jetbrains.annotations.NotNull;

public abstract class IndexedRecipeLookup extends AbstractRecipeLookup {

    /**
     * @throws ArrayIndexOutOfBoundsException if {@code index} equals or exceeds {@link #getRecipeCount()}
     * @param index the index to get the recipe for.
     * @return the recipe at the index.
     */
    public abstract @NotNull Recipe getRecipeByIndex(int index);
}
