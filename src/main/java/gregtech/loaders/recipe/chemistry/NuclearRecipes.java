package gregtech.loaders.recipe.chemistry;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;
import static gregtech.api.unification.ore.OrePrefix.ingotHot;

public class NuclearRecipes {

    public static void init() {
        // uranium isotopes
        CHEMICAL_RECIPES.recipeBuilder().duration(200).volts(VA[LV])
                .inputItem(dust, Uraninite, 3)
                .fluidInputs(HydrofluoricAcid.getFluid(4000))
                .fluidInputs(Fluorine.getFluid(2000))
                .fluidOutputs(UraniumHexafluoride.getFluid(1000))
                .fluidOutputs(Water.getFluid(2000))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(160).volts(VA[HV])
                .fluidInputs(UraniumHexafluoride.getFluid(1000))
                .fluidOutputs(EnrichedUraniumHexafluoride.getFluid(100))
                .fluidOutputs(DepletedUraniumHexafluoride.getFluid(900))
                .buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder().duration(160).volts(VA[MV])
                .fluidInputs(UraniumHexafluoride.getFluid(1000))
                .outputItem(dust, Uranium)
                .fluidOutputs(Fluorine.getFluid(6000))
                .buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder().duration(160).volts(VA[MV])
                .fluidInputs(EnrichedUraniumHexafluoride.getFluid(1000))
                .outputItem(dust, Uranium235)
                .fluidOutputs(Fluorine.getFluid(6000))
                .buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder().duration(160).volts(VA[MV])
                .fluidInputs(DepletedUraniumHexafluoride.getFluid(1000))
                .outputItem(dust, Uranium238)
                .fluidOutputs(Fluorine.getFluid(6000))
                .buildAndRegister();

        // zirconium and hafnium
        // ZrSiO4 -> ZrO2 + SiO2
        BLAST_RECIPES.recipeBuilder().duration(200).volts(VA[MV]).blastFurnaceTemp(3073)
                .inputItem(dust, Zircon, 6)
                .outputItem(dust, SiliconDioxide, 3)
                .outputItem(dust, Zirconia, 3).outputItemRoll(dust, Hafnia, 3333, 0)
                .buildAndRegister();

        // ZrO2 + C + 4Cl -> ZrCl4 + CO2
        CHEMICAL_RECIPES.recipeBuilder().duration(400).volts(VA[HV])
                .inputItem(dust, Zirconia, 3)
                .inputItem(dust, Carbon)
                .fluidInputs(Chlorine.getFluid(4000))
                .outputItem(dust, ZirconiumTetrachloride, 5)
                .fluidOutputs(CarbonDioxide.getFluid(1000))
                .buildAndRegister();

        // ZrCl4 + 2Mg -> Zr + 2MgCl2
        BLAST_RECIPES.recipeBuilder().duration(800).volts(VA[EV]).blastFurnaceTemp(2125)
                .inputItem(dust, ZirconiumTetrachloride, 5)
                .inputItem(dust, Magnesium, 2)
                .outputItem(ingotHot, Zirconium)
                .outputItem(dust, MagnesiumChloride, 6)
                .buildAndRegister();

        // HfO2 + C + 4Cl -> HfCl4 + CO2
        CHEMICAL_RECIPES.recipeBuilder().duration(400).volts(VA[HV])
                .inputItem(dust, Hafnia, 3)
                .inputItem(dust, Carbon)
                .fluidInputs(Chlorine.getFluid(4000))
                .outputItem(dust, HafniumTetrachloride, 5)
                .fluidOutputs(CarbonDioxide.getFluid(1000))
                .buildAndRegister();

        // HfCl4 + 2Mg -> Hf + 2MgCl2
        BLAST_RECIPES.recipeBuilder().duration(2000).volts(VA[EV]).blastFurnaceTemp(2227)
                .inputItem(dust, HafniumTetrachloride, 5)
                .inputItem(dust, Magnesium, 2)
                .outputItem(ingotHot, Hafnium)
                .outputItem(dust, MagnesiumChloride, 6)
                .buildAndRegister();
    }
}
