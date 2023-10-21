package gregtech.loaders.recipe.chemistry;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;

public class NuclearRecipes {

    public static void init() {

        // Thorium Fuel

        CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(dust, Thorium, 1)
                .fluidInputs(Fluorine.getFluid(4000))
                .output(dust, ThoriumTetrafluoride, 5)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(100).EUt(VA[LV])
                .input(dust, Lithium, 1)
                .fluidInputs(Fluorine.getFluid(1000))
                .output(dust, LithiumFluoride, 2)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(dust, Beryllium, 1)
                .fluidInputs(Fluorine.getFluid(2000))
                .output(dust, BerylliumFluoride, 3)
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(200).EUt([EV]).blastFurnaceTemp(1000)
                .input(dust, ThoriumTetrafluoride, 5)
                .input(dust, LithiumFluoride, 4)
                .input(dust, BerylliumFluoride, 3)
                .fluidOutputs(ThoriumFLiBe.getFluid(4000))
                .buildAndRegister();

        // Uranium enrichment

        CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(dust, Uraninite, 3)
                .fluidInputs(HydrofluoricAcid.getFluid(4000))
                .fluidInputs(Fluorine.getFluid(2000))
                .fluidOutputs(UraniumHexafluoride.getFluid(1000))
                .fluidOutputs(Water.getFluid(2000))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(160).EUt(VA[HV])
                .fluidInputs(UraniumHexafluoride.getFluid(1000))
                .fluidOutputs(EnrichedUraniumHexafluoride.getFluid(100))
                .fluidOutputs(DepletedUraniumHexafluoride.getFluid(900))
                .buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder().duration(160).EUt(VA[MV])
                .fluidInputs(EnrichedUraniumHexafluoride.getFluid(1000))
                .output(dust, Uranium235)
                .fluidOutputs(Fluorine.getFluid(6000))
                .buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder().duration(160).EUt(VA[MV])
                .fluidInputs(DepletedUraniumHexafluoride.getFluid(1000))
                .output(dust, Uranium238)
                .fluidOutputs(Fluorine.getFluid(6000))
                .buildAndRegister();

    }
}

// REGISTER LIST
/*
- Thorium tetrafluoride
- Lithium fluoride
- Beryllium fluoride
*/