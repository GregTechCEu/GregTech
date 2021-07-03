package gregtech.loaders.recipe.chemistry;

import gregtech.api.recipes.ingredients.IntCircuitIngredient;

import static gregtech.api.recipes.RecipeMaps.LARGE_CHEMICAL_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class LCRCombined {

    static void init() {
        LARGE_CHEMICAL_RECIPES.recipeBuilder()
            .fluidInputs(Epichlorohydrin.getFluid(1000))
            .fluidInputs(Phenol.getFluid(2000))
            .fluidInputs(Acetone.getFluid(1000))
            .fluidInputs(HydrochloricAcid.getFluid(1000))
            .input(dust, SodiumHydroxide, 3)
            .fluidOutputs(Epoxy.getFluid(1000))
            .fluidOutputs(SaltWater.getFluid(1000))
            .fluidOutputs(DilutedHydrochloricAcid.getFluid(1000))
            .notConsumable(new IntCircuitIngredient(27))
            .EUt(30)
            .duration(24*20)
            .buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Chlorine.getFluid(2000))
                .fluidInputs(Propene.getFluid(1000))
                .fluidInputs(HypochlorousAcid.getFluid(1000))
                .input(dust, SodiumHydroxide, 3)
                .fluidOutputs(HydrochloricAcid.getFluid(1000))
                .fluidOutputs(Epichlorohydrin.getFluid(1000))
                .fluidOutputs(SaltWater.getFluid(1000))
                .notConsumable(new IntCircuitIngredient(27))
                .EUt(30)
                .duration(24*20)
                .buildAndRegister();
    }
}
