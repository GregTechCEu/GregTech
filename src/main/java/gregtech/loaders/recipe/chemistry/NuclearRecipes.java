package gregtech.loaders.recipe.chemistry;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

import static gregtech.common.items.MetaItems.*;

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

        BLAST_RECIPES.recipeBuilder().duration(200).EUt(VA[EV]).blastFurnaceTemp(1000)
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

        GAS_CENTRIFUGE_RECIPES.recipeBuilder().duration(160).EUt(VA[HV])
                .fluidInputs(UraniumHexafluoride.getFluid(1000))
                .fluidOutputs(LowEnrichedUraniumHexafluoride.getFluid(100))
                .fluidOutputs(DepletedUraniumHexafluoride.getFluid(900))
                .buildAndRegister();

        // Fuel rod fabrication

                // Zircaloy
                BLAST_RECIPES.recipeBuilder().duration(200).EUt(VA[EV]).blastFurnaceTemp(2100)
                        .input(dust, Zircon, 1)
                        .output(dust, SiliconDioxide, 3)
                        .chancedOutput(dust, ZirconiumDioxide, 3, 9000, 0)
                        .chancedOutput(dust, HafniumDioxide, 3, 1000, 0)
                        .buildAndRegister();

                BLAST_RECIPES.recipeBuilder().duration(200).EUt(VA[EV]).blastFurnaceTemp(1400)
                        .input(dust, ZirconiumDioxide, 1)
                        .input(dust, Carbon, 1)
                        .fluidInputs(Chlorine.getFluid(4000))
                        .fluidOutputs(CarbonDioxide.getFluid(1000))
                        .output(dust, ZirconiumTetrachloride, 5)
                        .buildAndRegister();

                BLAST_RECIPES.recipeBuilder().duration(200).EUt(VA[EV]).blastFurnaceTemp(1250)
                        .input(dust, HafniumDioxide, 1)
                        .input(dust, Carbon, 1)
                        .fluidInputs(Chlorine.getFluid(4000))
                        .fluidOutputs(CarbonDioxide.getFluid(1000))
                        .output(dust, HafniumTetrachloride, 5)
                        .buildAndRegister();

                BLAST_RECIPES.recipeBuilder().duration(200).EUt(VA[EV]).blastFurnaceTemp(1150)
                        .input(dust, ZirconiumTetrachloride, 5)
                        .input(dust, Magnesium, 2)
                        .output(dust, Zirconium, 1)
                        .output(dust, MagnesiumChloride, 6)
                        .buildAndRegister();

                BLAST_RECIPES.recipeBuilder().duration(200).EUt(VA[EV]).blastFurnaceTemp(1150)
                        .input(dust, HafniumTetrachloride, 5)
                        .input(dust, Magnesium, 2)
                        .output(dust, Hafnium, 1)
                        .output(dust, MagnesiumChloride, 6)
                        .buildAndRegister();

                MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV])
                        .input(dust, Zirconium, 1)
                        .fluidInputs(Tin.getFluid(15))
                        .fluidInputs(Chrome.getFluid(1))
                        .output(dust, Zircaloy, 1)
                        .buildAndRegister();

                // LEU-235 Dioxide
                CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                        .fluidInputs(LowEnrichedUraniumHexafluoride.getFluid(1000))
                        .fluidInputs(Water.getFluid(2000))
                        .fluidInputs(Hydrogen.getFluid(2000))
                        .output(dust, LowEnrichedUraniumDioxide, 1)
                        .fluidOutputs(HydrofluoricAcid.getFluid(6000))
                        .buildAndRegister();

                CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                        .fluidInputs(DepletedUraniumHexafluoride.getFluid(1000))
                        .fluidInputs(Water.getFluid(2000))
                        .fluidInputs(Hydrogen.getFluid(2000))
                        .output(dust, DepletedUraniumDioxide, 1)
                        .fluidOutputs(HydrofluoricAcid.getFluid(6000))
                        .buildAndRegister();

                FORMING_PRESS_RECIPES.recipeBuilder().duration(25).EUt(VA[EV])
                        .input(dust, LowEnrichedUraniumDioxide, 1)
                        .output(FUEL_PELLET_LEU235)
                        .buildAndRegister();

                ASSEMBLER_RECIPES.recipeBuilder().duration(800).EUt(VA[EV])
                        .input(plate, Zircaloy, 4)
                        .input(spring, Zircaloy, 1)
                        .input(FUEL_PELLET_LEU235, 16)
                        .output(FUEL_ROD_LEU235)
                        .buildAndRegister();

                // Natural Uranium Fuel

                FORMING_PRESS_RECIPES.recipeBuilder().duration(25).EUt(VA[EV])
                        .input(dust, Uraninite, 1)
                        .output(FUEL_PELLET_NATURAL_URANIUM)
                        .buildAndRegister();

                ASSEMBLER_RECIPES.recipeBuilder().duration(800).EUt(VA[EV])
                        .input(plate, Zircaloy, 4)
                        .input(FUEL_PELLET_NATURAL_URANIUM, 16)
                        .output(FUEL_ROD_NATURAL_RUANIUM)
                        .buildAndRegister();
    }
}

// REGISTER LIST
/*
- Thorium tetrafluoride
- Lithium fluoride
- Beryllium fluoride
*/
