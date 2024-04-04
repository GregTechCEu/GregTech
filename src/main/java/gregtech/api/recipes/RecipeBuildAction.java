package gregtech.api.recipes;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface RecipeBuildAction<R extends RecipeBuilder<R>> {

    /**
     * Process a RecipeBuilder to perform an action with.
     * <p>
     * <strong>Do not call {@link RecipeBuilder#buildAndRegister()} on the passed builder.</strong>
     * It is safe to do so only on other builders, i.e. created through {@link RecipeBuilder#copy()}.
     *
     * @param builder the builder to utilize
     */
    void accept(@NotNull R builder);
}
