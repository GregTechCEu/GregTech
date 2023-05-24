package gregtech.loaders.recipe.chemistry;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;

public class FuelRecipeChains {

    public static void init() {

        // High Octane Gasoline
        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Naphtha.getFluid(16000))
                .fluidInputs(RefineryGas.getFluid(2000))
                .fluidInputs(Methanol.getFluid(1000))
                .fluidInputs(Acetone.getFluid(1000))
                .circuitMeta(24)
                .fluidOutputs(RawGasoline.getFluid(20000))
                .duration(100).EUt(VA[HV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(RawGasoline.getFluid(10000))
                .fluidInputs(Toluene.getFluid(1000))
                .fluidOutputs(Gasoline.getFluid(11000))
                .duration(10).EUt(VA[HV]).buildAndRegister();

        // Nitrous Oxide
        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Nitrogen.getFluid(2000))
                .fluidInputs(Oxygen.getFluid(1000))
                .circuitMeta(4)
                .fluidOutputs(NitrousOxide.getFluid(1000))
                .duration(100).EUt(VA[LV]).buildAndRegister();

        // Ethyl Tert-Butyl Ether
        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Butene.getFluid(1000))
                .fluidInputs(Ethanol.getFluid(1000))
                .fluidOutputs(EthylTertButylEther.getFluid(1000))
                .duration(400).EUt(VA[HV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Gasoline.getFluid(20000))
                .fluidInputs(Octane.getFluid(2000))
                .fluidInputs(NitrousOxide.getFluid(2000))
                .fluidInputs(Toluene.getFluid(1000))
                .fluidInputs(EthylTertButylEther.getFluid(1000))
                .circuitMeta(24)
                .fluidOutputs(HighOctaneGasoline.getFluid(32000))
                .duration(50).EUt(VA[EV]).buildAndRegister();

        //LPG
        VACUUM_RECIPES.recipeBuilder()
                .fluidInputs(RefineryGas.getFluid(1000))
                .fluidOutputs(LPG.getFluid(1000))
                .duration(75).EUt(VA[MV]).buildAndRegister();
    }
}
