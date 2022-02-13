package gregtech.loaders.recipe.chemistry;

import gregtech.api.recipes.ingredients.IntCircuitIngredient;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.CHEMICAL_RECIPES;
import static gregtech.api.recipes.RecipeMaps.LARGE_CHEMICAL_RECIPES;
import static gregtech.api.unification.material.Materials.*;

public class FuelRecipeChains {

    public static void init() {

        // High Octane Gasoline
        LARGE_CHEMICAL_RECIPES.recipeBuilder().EUt(VA[HV]).duration(100)
                .fluidInputs(Naphtha.getFluid(16000))
                .fluidInputs(RefineryGas.getFluid(2000))
                .fluidInputs(Methanol.getFluid(1000))
                .fluidInputs(Acetone.getFluid(1000))
                .notConsumable(new IntCircuitIngredient(24))
                .fluidOutputs(RawGasoline.getFluid(20000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[HV]).duration(10)
                .fluidInputs(RawGasoline.getFluid(10000))
                .fluidInputs(Toluene.getFluid(1000))
                .fluidOutputs(Gasoline.getFluid(11000))
                .buildAndRegister();

        // Nitrous Oxide
        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[LV]).duration(100)
                .fluidInputs(Nitrogen.getFluid(2000))
                .fluidInputs(Oxygen.getFluid(1000))
                .notConsumable(new IntCircuitIngredient(4))
                .fluidOutputs(NitrousOxide.getFluid(1000))
                .buildAndRegister();

        // Ethyl Tert-Butyl Ether
        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[HV]).duration(400)
                .fluidInputs(Butene.getFluid(1000))
                .fluidInputs(Ethanol.getFluid(1000))
                .fluidOutputs(EthylTertButylEther.getFluid(1000))
                .buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder().EUt(VA[EV]).duration(50)
                .fluidInputs(Gasoline.getFluid(20000))
                .fluidInputs(Octane.getFluid(2000))
                .fluidInputs(NitrousOxide.getFluid(2000))
                .fluidInputs(Toluene.getFluid(1000))
                .fluidInputs(EthylTertButylEther.getFluid(1000))
                .notConsumable(new IntCircuitIngredient(24))
                .fluidOutputs(HighOctaneGasoline.getFluid(32000))
                .buildAndRegister();

        // Nitrobenzene
        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[HV]).duration(160)
                .fluidInputs(Benzene.getFluid(5000))
                .fluidInputs(NitrationMixture.getFluid(2000))
                .fluidInputs(DistilledWater.getFluid(2000))
                .fluidOutputs(Nitrobenzene.getFluid(8000))
                .fluidOutputs(DilutedSulfuricAcid.getFluid(1000))
                .buildAndRegister();
    }
}
