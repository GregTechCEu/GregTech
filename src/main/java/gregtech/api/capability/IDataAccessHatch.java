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
     * @return true if this Data Access Hatch is creative or not
     */
    boolean isCreative();

    /**
     * @return if this hatch receives data from cables
     */
    default boolean isReceiver() {
        return false;
    }

    /**
     * @return if this hatch transmits data through cables
     */
    default boolean isTransmitter() {
        return false;
    }
}
