package gregtech.loaders.recipe;

import gregtech.api.recipes.ingredients.IntCircuitIngredient;

import static gregtech.api.recipes.RecipeMaps.LARGE_CHEMICAL_RECIPES;
import static gregtech.api.unification.material.Materials.*;

public class LargeReactorRecipes {

    public static void init() {
        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(24))
                .fluidInputs(Methane.getFluid(3000))
                .fluidInputs(Nitrogen.getFluid(4000))
                .fluidInputs(Oxygen.getFluid(3000))
                .fluidOutputs(Ammonia.getFluid(4000))
                .fluidOutputs(CarbonMonoxide.getFluid(3000))
                .duration(320).EUt(30).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(24))
                .fluidInputs(Hydrogen.getFluid(6000))
                .fluidInputs(CarbonDioxide.getFluid(1000))
                .fluidOutputs(Methane.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000))
                .duration(160).EUt(30).buildAndRegister();
    }
}
