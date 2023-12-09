package gregtech.api.recipes.machines;

import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ui.RecipeMapUIFunction;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public class RecipeMapResearchStation<R extends RecipeBuilder<R>> extends RecipeMap<R> implements IScannerRecipeMap {

    public RecipeMapResearchStation(@NotNull String unlocalizedName, @NotNull R defaultRecipeBuilder,
                                    @NotNull RecipeMapUIFunction recipeMapUI) {
        super(unlocalizedName, defaultRecipeBuilder, recipeMapUI, 2, 1, 0, 0);
    }
}
