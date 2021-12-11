package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipes.FuelRecipe2;
import gregtech.api.util.ValidationResult;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;

public class FuelRecipeBuilder extends RecipeBuilder<FuelRecipeBuilder> {

    private final HashMap<FluidStack, Integer[]> boostFluids = new HashMap<>();

    public FuelRecipeBuilder() {

    }

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

    public FuelRecipeBuilder boostFluid(FluidStack fluidStack, int duration, int boostFactor) {
        boostFluids.put(fluidStack, new Integer[]{duration, boostFactor});
        return this;
    }

    public ValidationResult<Recipe> build() {
        return ValidationResult.newResult(finalizeAndValidate(),
                new FuelRecipe2(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs, boostFluids, duration, EUt, hidden));
    }
}
