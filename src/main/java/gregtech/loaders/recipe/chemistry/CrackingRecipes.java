package gregtech.loaders.recipe.chemistry;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.CRACKING_RECIPES;
import static gregtech.api.unification.material.Materials.*;

public class CrackingRecipes {

    public static void init() {
        CRACKING_RECIPES.recipeBuilder()
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidInputs(Ethane.getFluid(1000))
                .fluidOutputs(HydroCrackedEthane.getFluid(1000))
                .duration(40).EUt(VA[MV]).buildAndRegister();

        CRACKING_RECIPES.recipeBuilder()
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidInputs(Ethylene.getFluid(1000))
                .fluidOutputs(HydroCrackedEthylene.getFluid(1000))
                .duration(40).EUt(VA[MV]).buildAndRegister();

        CRACKING_RECIPES.recipeBuilder()
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidInputs(Propene.getFluid(1000))
                .fluidOutputs(HydroCrackedPropene.getFluid(1000))
                .duration(40).EUt(VA[MV]).buildAndRegister();

        CRACKING_RECIPES.recipeBuilder()
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidInputs(Propane.getFluid(1000))
                .fluidOutputs(HydroCrackedPropane.getFluid(1000))
                .duration(40).EUt(VA[MV]).buildAndRegister();

        CRACKING_RECIPES.recipeBuilder()
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidInputs(LightFuel.getFluid(1000))
                .fluidOutputs(HydroCrackedLightFuel.getFluid(1000))
                .duration(40).EUt(VA[MV]).buildAndRegister();

        CRACKING_RECIPES.recipeBuilder()
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidInputs(Butane.getFluid(1000))
                .fluidOutputs(HydroCrackedButane.getFluid(1000))
                .duration(40).EUt(VA[MV]).buildAndRegister();

        CRACKING_RECIPES.recipeBuilder()
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidInputs(Naphtha.getFluid(1000))
                .fluidOutputs(HydroCrackedNaphtha.getFluid(1000))
                .duration(40).EUt(VA[MV]).buildAndRegister();

        CRACKING_RECIPES.recipeBuilder()
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidInputs(HeavyFuel.getFluid(1000))
                .fluidOutputs(HydroCrackedHeavyFuel.getFluid(1000))
                .duration(40).EUt(VA[MV]).buildAndRegister();

        CRACKING_RECIPES.recipeBuilder()
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidInputs(RefineryGas.getFluid(1000))
                .fluidOutputs(HydroCrackedGas.getFluid(1000))
                .duration(40).EUt(VA[MV]).buildAndRegister();

        CRACKING_RECIPES.recipeBuilder()
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidInputs(Butene.getFluid(1000))
                .fluidOutputs(HydroCrackedButene.getFluid(1000))
                .duration(40).EUt(VA[MV]).buildAndRegister();

        CRACKING_RECIPES.recipeBuilder()
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidInputs(Butadiene.getFluid(1000))
                .fluidOutputs(HydroCrackedButadiene.getFluid(1000))
                .duration(40).EUt(VA[MV]).buildAndRegister();

        CRACKING_RECIPES.recipeBuilder()
                .fluidInputs(Steam.getFluid(2000))
                .fluidInputs(Ethane.getFluid(1000))
                .fluidOutputs(SteamCrackedEthane.getFluid(1000))
                .duration(40).EUt(VA[MV]).buildAndRegister();

        CRACKING_RECIPES.recipeBuilder()
                .fluidInputs(Steam.getFluid(2000))
                .fluidInputs(Ethylene.getFluid(1000))
                .fluidOutputs(SteamCrackedEthylene.getFluid(1000))
                .duration(40).EUt(VA[MV]).buildAndRegister();

        CRACKING_RECIPES.recipeBuilder()
                .fluidInputs(Steam.getFluid(2000))
                .fluidInputs(Propene.getFluid(1000))
                .fluidOutputs(SteamCrackedPropene.getFluid(1000))
                .duration(40).EUt(VA[MV]).buildAndRegister();

        CRACKING_RECIPES.recipeBuilder()
                .fluidInputs(Steam.getFluid(2000))
                .fluidInputs(Propane.getFluid(1000))
                .fluidOutputs(SteamCrackedPropane.getFluid(1000))
                .duration(40).EUt(VA[MV]).buildAndRegister();

        CRACKING_RECIPES.recipeBuilder()
                .fluidInputs(Steam.getFluid(2000))
                .fluidInputs(LightFuel.getFluid(1000))
                .fluidOutputs(SteamCrackedLightFuel.getFluid(1000))
                .duration(40).EUt(VA[MV]).buildAndRegister();

        CRACKING_RECIPES.recipeBuilder()
                .fluidInputs(Steam.getFluid(2000))
                .fluidInputs(Butane.getFluid(1000))
                .fluidOutputs(SteamCrackedButane.getFluid(1000))
                .duration(40).EUt(VA[MV]).buildAndRegister();

        CRACKING_RECIPES.recipeBuilder()
                .fluidInputs(Steam.getFluid(2000))
                .fluidInputs(Naphtha.getFluid(1000))
                .fluidOutputs(SteamCrackedNaphtha.getFluid(1000))
                .duration(40).EUt(VA[MV]).buildAndRegister();

        CRACKING_RECIPES.recipeBuilder()
                .fluidInputs(Steam.getFluid(2000))
                .fluidInputs(HeavyFuel.getFluid(1000))
                .fluidOutputs(SteamCrackedHeavyFuel.getFluid(1000))
                .duration(40).EUt(VA[MV]).buildAndRegister();

        CRACKING_RECIPES.recipeBuilder()
                .fluidInputs(Steam.getFluid(2000))
                .fluidInputs(RefineryGas.getFluid(1000))
                .fluidOutputs(SteamCrackedGas.getFluid(1000))
                .duration(40).EUt(VA[MV]).buildAndRegister();

        CRACKING_RECIPES.recipeBuilder()
                .fluidInputs(Steam.getFluid(2000))
                .fluidInputs(Butene.getFluid(1000))
                .fluidOutputs(SteamCrackedButene.getFluid(1000))
                .duration(40).EUt(VA[MV]).buildAndRegister();

        CRACKING_RECIPES.recipeBuilder()
                .fluidInputs(Steam.getFluid(2000))
                .fluidInputs(Butadiene.getFluid(1000))
                .fluidOutputs(SteamCrackedButadiene.getFluid(1000))
                .duration(40).EUt(VA[MV]).buildAndRegister();
    }
}
