package gregtech.api.capability;

import gregtech.api.recipes.Recipe;

import java.util.Set;

public interface IDataAccessHatch {

    /**
     *
     * @return all recipes which are allowed to be run in the main multiblock
     */
    Set<Recipe> getAvailableRecipes();

    /**
     *
     * @return true if this Data Access Hatch is creative or not
     */
    boolean isCreative();
}
