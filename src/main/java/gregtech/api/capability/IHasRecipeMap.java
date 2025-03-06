package gregtech.api.capability;

import gregtech.api.recipes.RecipeMap;

import org.jetbrains.annotations.Nullable;

public interface IHasRecipeMap {

    @Nullable
    RecipeMap<?> getRecipeMap();
}
