package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.ValidationResult;

public class SimpleRecipeBuilder extends RecipeBuilder<SimpleRecipeBuilder> {

    public SimpleRecipeBuilder() {
    }

    public SimpleRecipeBuilder(Recipe recipe, RecipeMap<SimpleRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public SimpleRecipeBuilder(RecipeBuilder<SimpleRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public SimpleRecipeBuilder copy() {
        return new SimpleRecipeBuilder(this);
    }

    public ValidationResult<Recipe> build() {
        return ValidationResult.newResult(finalizeAndValidate(),
                new Recipe(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs, duration, EUt, hidden, isCTRecipe));
    }
}
