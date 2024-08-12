package gregtech.loaders.recipe.chemistry;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;
import static gregtech.api.unification.ore.OrePrefix.ingotHot;

public class NuclearRecipes {

    public static void init() {
        // uranium isotopes
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
                .fluidInputs(UraniumHexafluoride.getFluid(1000))
                .output(dust, Uranium)
                .fluidOutputs(Fluorine.getFluid(6000))
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

        // zirconium and hafnium
        // ZrSiO4 -> ZrO2 + SiO2
        BLAST_RECIPES.recipeBuilder().duration(200).EUt(VA[MV]).blastFurnaceTemp(3073)
                .input(dust, Zircon, 6)
                .output(dust, SiliconDioxide, 3)
                .output(dust, Zirconia, 3)
                .chancedOutput(dust, Hafnia, 3333, 0)
                .buildAndRegister();

        // ZrO2 + C + 4Cl -> ZrCl4 + CO2
        CHEMICAL_RECIPES.recipeBuilder().duration(400).EUt(VA[HV])
                .input(dust, Zirconia, 3)
                .input(dust, Carbon)
                .fluidInputs(Chlorine.getFluid(4000))
                .output(dust, ZirconiumTetrachloride, 5)
                .fluidOutputs(CarbonDioxide.getFluid(1000))
                .buildAndRegister();

        // ZrCl4 + 2Mg -> Zr + 2MgCl2
        BLAST_RECIPES.recipeBuilder().duration(800).EUt(VA[EV]).blastFurnaceTemp(2125)
                .input(dust, ZirconiumTetrachloride, 5)
                .input(dust, Magnesium, 2)
                .output(ingotHot, Zirconium)
                .output(dust, MagnesiumChloride, 6)
                .buildAndRegister();

        // HfO2 + C + 4Cl -> HfCl4 + CO2
        CHEMICAL_RECIPES.recipeBuilder().duration(400).EUt(VA[HV])
                .input(dust, Hafnia, 3)
                .input(dust, Carbon)
                .fluidInputs(Chlorine.getFluid(4000))
                .output(dust, HafniumTetrachloride, 5)
                .fluidOutputs(CarbonDioxide.getFluid(1000))
                .buildAndRegister();

        // HfCl4 + 2Mg -> Hf + 2MgCl2
        BLAST_RECIPES.recipeBuilder().duration(2000).EUt(VA[EV]).blastFurnaceTemp(2227)
                .input(dust, HafniumTetrachloride, 5)
                .input(dust, Magnesium, 2)
                .output(ingotHot, Hafnium)
                .output(dust, MagnesiumChloride, 6)
                .buildAndRegister();
    }
}
