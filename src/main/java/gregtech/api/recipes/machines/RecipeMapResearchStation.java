package gregtech.api.recipes.machines;

import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ui.RecipeMapUIFunction;
import gregtech.core.sound.GTSoundEvents;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public class RecipeMapResearchStation<R extends RecipeBuilder<R>> extends RecipeMap<R> implements IScannerRecipeMap {

    public RecipeMapResearchStation(@NotNull String unlocalizedName, @NotNull R defaultRecipeBuilder,
                                    @NotNull RecipeMapUIFunction recipeMapUI) {
        super(unlocalizedName, defaultRecipeBuilder, recipeMapUI, 2, 1, 0, 0);
        setSound(GTValues.isAprilFools() ? GTSoundEvents.SCIENCE : GTSoundEvents.COMPUTATION);
        getPrimaryRecipeCategory().jeiSortToBack(true);
    }
}
