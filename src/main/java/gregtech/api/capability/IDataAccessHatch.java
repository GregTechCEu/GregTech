package gregtech.api.capability;

import gregtech.api.recipes.Recipe;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public interface IDataAccessHatch {

    /**
     * If passed a {@code seen} context, you must use {@link #isRecipeAvailable(Recipe, Collection)} to prevent
     * infinite recursion
     *
     * @param recipe the recipe to check
     * @return if the recipe is available for use
     */
    default boolean isRecipeAvailable(@NotNull Recipe recipe) {
        Collection<IDataAccessHatch> list = new ArrayList<>();
        list.add(this);
        return isRecipeAvailable(recipe, list);
    }

    /**
     * @param recipe the recipe to check
     * @param seen   the hatches already checked
     * @return if the recipe is available for use
     */
    boolean isRecipeAvailable(@NotNull Recipe recipe, @NotNull Collection<IDataAccessHatch> seen);

    /**
     * @return true if this Data Access Hatch is creative or not
     */
    boolean isCreative();
}
