package gregtech.api.capability;

import gregtech.api.recipes.Recipe;

import javax.annotation.Nonnull;

public interface IDataAccessHatch {

    /**
     * @param recipe the recipe to check
     * @return if the recipe is available for use
     */
    boolean isRecipeAvailable(@Nonnull Recipe recipe);

    /**
     *
     * @return true if this Data Access Hatch is creative or not
     */
    boolean isCreative();
}
