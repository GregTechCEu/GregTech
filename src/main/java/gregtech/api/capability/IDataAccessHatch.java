package gregtech.api.capability;

import gregtech.api.recipes.Recipe;

import javax.annotation.Nonnull;
import java.util.Set;

public interface IDataAccessHatch {

    /**
     *
     * @return all recipes which are allowed to be run in the main multiblock
     */
    @Nonnull
    Set<Recipe> getAvailableRecipes();

    /**
     *
     * @return true if this Data Access Hatch is creative or not
     */
    default boolean isCreative() {
        return false;
    }
}
