package gregtech.api.recipes.category;

import gregtech.api.recipes.RecipeMap;

import org.jetbrains.annotations.NotNull;

/**
 * When a registered mte's class or recipe logic implements this interface, the mte is associated with the override's
 * recipe maps in JEI.
 */
public interface ICategoryOverride {

    /**
     * Controls whether the override should be performed or not.
     * Useful to disable overrides if a class's parent implements this interface.
     *
     * @return whether the override should be performed or not
     */
    default boolean shouldOverride() {
        return true;
    }

    /**
     * Controls whether the normal logic for determining JEI category association should be completely replaced.
     *
     * @return whether to ignore normal logic or not
     */
    default boolean shouldReplace() {
        return true;
    }

    /**
     * The actual overrides for JEI recipe map category association.
     *
     * @return an array of recipe maps the mte should be associated with in JEI. Can be empty, but not null.
     */
    default @NotNull RecipeMap<?> @NotNull [] getJEIRecipeMapCategoryOverrides() {
        return new RecipeMap[] {};
    }

    /**
     * Allows JEI category association using JEI's underlying API.
     * Use this to associate a multiblock with solid, burnable fuel recipes for example.
     *
     * @return an array of recipe category UUIDs that are valid for JEI's
     *         {@link mezz.jei.api.IModRegistry#addRecipeCatalyst(Object, String...)} method.
     */
    default @NotNull String @NotNull [] getJEICategoryOverrides() {
        return new String[] {};
    }
}
