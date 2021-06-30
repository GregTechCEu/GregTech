package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ValidationResult;
import net.minecraftforge.fluids.FluidStack;

public class UniversalDistillationRecipeBuilder extends RecipeBuilder<UniversalDistillationRecipeBuilder> {

    public UniversalDistillationRecipeBuilder() {
    }

    public UniversalDistillationRecipeBuilder(Recipe recipe, RecipeMap<UniversalDistillationRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public UniversalDistillationRecipeBuilder(RecipeBuilder<UniversalDistillationRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public UniversalDistillationRecipeBuilder copy() {
        return new UniversalDistillationRecipeBuilder(this);
    }

    @Override
    public void buildAndRegister() {
        IntCircuitRecipeBuilder builder = RecipeMaps.DISTILLERY_RECIPES.recipeBuilder()
                .EUt(this.EUt / 4);

        //todo do this on a per-output basis
        int ratio = GTUtility.getRatioForDistillation(this.fluidInputs, this.fluidOutputs, this.outputs);

        for (int i = 0; i < fluidOutputs.size(); i++) {
            builder.copy()
                    .circuitMeta(i + 1)
                    .fluidInputs(new FluidStack(this.fluidInputs.get(0), this.fluidInputs.get(0).amount / ratio)) //todo fluid inputs must not be reduced to smaller than 25mB
                    .fluidOutputs(new FluidStack(this.fluidOutputs.get(i), this.fluidOutputs.get(i).amount / ratio))
                    .duration(this.EUt > 16 ? (int) (this.duration * 2.8f / ratio) : this.duration * 2 / ratio)
                    //todo figure out how to do outputs without creating broken recipes
                    .outputs(this.outputs)
                    .buildAndRegister();
        }

        super.buildAndRegister();
    }

    public ValidationResult<Recipe> build() {
        return ValidationResult.newResult(finalizeAndValidate(),
                new Recipe(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs, duration, EUt, hidden));
    }

}
