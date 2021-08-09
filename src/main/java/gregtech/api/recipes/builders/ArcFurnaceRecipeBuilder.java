package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.ValidationResult;

public class ArcFurnaceRecipeBuilder extends RecipeBuilder<ArcFurnaceRecipeBuilder> {

    public ArcFurnaceRecipeBuilder() {
    }

    public ArcFurnaceRecipeBuilder(Recipe recipe, RecipeMap<ArcFurnaceRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public ArcFurnaceRecipeBuilder(RecipeBuilder<ArcFurnaceRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public ArcFurnaceRecipeBuilder copy() {
        return new ArcFurnaceRecipeBuilder(this);
    }

    @Override
    public void buildAndRegister() {
        if (fluidInputs.isEmpty()) {
            fluidInputs(Materials.Oxygen.getFluid(this.duration));
            for (Material material : new Material[]{Materials.Argon, Materials.Nitrogen}) {
                int plasmaAmount = (int) Math.max(1L, this.duration / (material.getAverageMass() * 16L));
                RecipeMaps.ARC_FURNACE_RECIPES.recipeBuilder()
                        .inputsIngredients(this.inputs)
                        .outputs(this.outputs)
                        .duration(Math.max(1, this.duration / 16))
                        .EUt(this.EUt / 3)
                        .fluidInputs(material.getPlasma(plasmaAmount))
                        .fluidOutputs(material.getFluid(plasmaAmount))
                        .buildAndRegister();
            }
        }
        super.buildAndRegister();
    }

    public ValidationResult<Recipe> build() {
        return ValidationResult.newResult(finalizeAndValidate(),
                new Recipe(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs, duration, EUt, hidden));
    }

}
