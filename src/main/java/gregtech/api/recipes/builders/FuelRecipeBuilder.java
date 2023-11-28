package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;

public class FuelRecipeBuilder extends RecipeBuilder<FuelRecipeBuilder> {

    public FuelRecipeBuilder() {}

    public FuelRecipeBuilder(Recipe recipe, RecipeMap<FuelRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public FuelRecipeBuilder(RecipeBuilder<FuelRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public FuelRecipeBuilder copy() {
        return new FuelRecipeBuilder(this);
    }
}
