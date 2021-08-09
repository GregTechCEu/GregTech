package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.ValidationResult;

public class CutterRecipeBuilder extends RecipeBuilder<CutterRecipeBuilder> {

    public CutterRecipeBuilder() {
    }

    public CutterRecipeBuilder(Recipe recipe, RecipeMap<CutterRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public CutterRecipeBuilder(RecipeBuilder<CutterRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public CutterRecipeBuilder copy() {
        return new CutterRecipeBuilder(this);
    }

    @Override
    public ValidationResult<Recipe> build() {
        return ValidationResult.newResult(finalizeAndValidate(),
                new Recipe(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs, duration, EUt, hidden));
    }

    @Override
    public void buildAndRegister() {
        if (fluidInputs.isEmpty()) {
            recipeMap.addRecipe(this.copy()
                    .fluidInputs(Materials.Water.getFluid(Math.max(4, Math.min(1000, duration * EUt / 320))))
                    .duration(duration * 2).build());
            recipeMap.addRecipe(this.copy()
                    .fluidInputs(Materials.DistilledWater.getFluid(Math.max(3, Math.min(750, duration * EUt / 426))))
                    .duration((int) (duration * 1.5)).build());
            recipeMap.addRecipe(this.copy()
                    .fluidInputs(Materials.Lubricant.getFluid(Math.max(1, Math.min(250, duration * EUt / 1280))))
                    .duration(Math.max(1, duration)).build());
        } else {
            recipeMap.addRecipe(build());
        }
    }
}
