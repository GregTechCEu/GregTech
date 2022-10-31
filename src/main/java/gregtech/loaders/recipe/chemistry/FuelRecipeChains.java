package gregtech.loaders.recipe.chemistry;

import gregtech.api.recipes.ingredients.IntCircuitIngredient;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
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

        // Diesel
        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(LightFuel.getFluid(5000))
                .fluidInputs(HeavyFuel.getFluid(1000))
                .fluidOutputs(Diesel.getFluid(6000))
                .duration(16).EUt(VA[MV]).buildAndRegister();

        // Cetane-Boosted Diesel
        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(BioDiesel.getFluid(1000))
                .fluidInputs(Tetranitromethane.getFluid(40))
                .fluidOutputs(CetaneBoostedDiesel.getFluid(750))
                .duration(20).EUt(VA[HV]).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(Diesel.getFluid(1000))
                .fluidInputs(Tetranitromethane.getFluid(20))
                .fluidOutputs(CetaneBoostedDiesel.getFluid(1000))
                .duration(20).EUt(VA[HV]).buildAndRegister();

        // Rocket Fuel
        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidInputs(Dimethylhydrazine.getFluid(1000))
                .fluidOutputs(RocketFuel.getFluid(3000))
                .duration(60).EUt(16).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(DinitrogenTetroxide.getFluid(1000))
                .fluidInputs(Dimethylhydrazine.getFluid(1000))
                .fluidOutputs(RocketFuel.getFluid(6000))
                .duration(60).EUt(16).buildAndRegister();
    }
}
