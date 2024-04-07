package gregtech.loaders.recipe.chemistry;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;
import static gregtech.api.unification.ore.OrePrefix.plate;

public class NuclearRecipes {

    public static void init() {
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
                .output(dust, Uranium)
                .fluidOutputs(Fluorine.getFluid(6000))
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(420).EUt(VA[LV])
                .input(dust, DepletedUraniumDioxide, 20)
                .input(dust, EnrichedUraniumDioxide)
                .circuitMeta(1)
                .output(dust, LEU235, 21)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(120).EUt(VA[LV])
                .input(dust, DepletedUraniumDioxide, 5)
                .input(dust, EnrichedUraniumDioxide)
                .circuitMeta(2)
                .output(dust, HEU235, 6)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(420).EUt(VA[LV])
                .input(dust, Uraninite, 20)
                .circuitMeta(1)
                .input(dust, Plutonium239Dioxide)
                .output(dust, LowGradeMOX, 21)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(120).EUt(VA[LV])
                .input(dust, Uraninite, 5)
                .circuitMeta(2)
                .input(dust, Plutonium239Dioxide)
                .output(dust, HighGradeMOX, 6)
                .buildAndRegister();

/*
        BENDER_RECIPES.recipeBuilder().duration(400).EUt(VA[MV])
                .input(plate, Zircaloy)
                .circuitMeta(2)
                .outputs(MetaItems.)
*/
    }
}
