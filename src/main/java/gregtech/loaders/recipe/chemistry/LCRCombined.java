package gregtech.loaders.recipe.chemistry;

import gregtech.api.recipes.ingredients.IntCircuitIngredient;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.LARGE_CHEMICAL_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;

public class LCRCombined {

    static void init() {
        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(24))
                .fluidInputs(Epichlorohydrin.getFluid(1000))
                .fluidInputs(Phenol.getFluid(2000))
                .fluidInputs(Acetone.getFluid(1000))
                .fluidInputs(HydrochloricAcid.getFluid(1000))
                .input(dust, SodiumHydroxide, 3)
                .fluidOutputs(Epoxy.getFluid(1000))
                .fluidOutputs(SaltWater.getFluid(1000))
                .fluidOutputs(DilutedHydrochloricAcid.getFluid(1000))
                .EUt(VA[LV])
                .duration(24 * 20)
                .buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(24))
                .fluidInputs(Chlorine.getFluid(2000))
                .fluidInputs(Propene.getFluid(1000))
                .fluidInputs(HypochlorousAcid.getFluid(1000))
                .input(dust, SodiumHydroxide, 3)
                .fluidOutputs(HydrochloricAcid.getFluid(1000))
                .fluidOutputs(Epichlorohydrin.getFluid(1000))
                .fluidOutputs(SaltWater.getFluid(1000))
                .EUt(VA[LV])
                .duration(24 * 20)
                .buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(24))
                .fluidInputs(Methane.getFluid(3000))
                .fluidInputs(Nitrogen.getFluid(4000))
                .fluidInputs(Oxygen.getFluid(3000))
                .fluidOutputs(Ammonia.getFluid(4000))
                .fluidOutputs(CarbonMonoxide.getFluid(3000))
                .EUt(VA[HV])
                .duration(320)
                .buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(24))
                .fluidInputs(Hydrogen.getFluid(6000))
                .fluidInputs(CarbonDioxide.getFluid(1000))
                .fluidOutputs(Methane.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000))
                .EUt(VA[LV])
                .duration(160)
                .buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(24))
                .input(dust, Sulfur)
                .fluidInputs(Water.getFluid(4000))
                .fluidOutputs(SulfuricAcid.getFluid(1000))
                .EUt(VA[HV])
                .duration(320)
                .buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(24))
                .fluidInputs(Nitrogen.getFluid(1000))
                .fluidInputs(Hydrogen.getFluid(3000))
                .fluidInputs(Oxygen.getFluid(4000))
                .fluidOutputs(NitricAcid.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000))
                .EUt(VA[HV])
                .duration(320)
                .buildAndRegister();
    }
}
