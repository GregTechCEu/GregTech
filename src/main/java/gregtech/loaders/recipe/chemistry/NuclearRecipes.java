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
                .input(dust, Uraninite, 1)
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
                        .input(dust, Zirconium, 16)
                        .fluidInputs(Tin.getFluid(32))
                        .fluidInputs(Chrome.getFluid(2))
                        .output(dust, Zircaloy, 16)
                        .buildAndRegister();

                // Inconel 718
                MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV])
                        .input(dust, Nickel, 5)
                        .input(dust, Chromium, 2)
                        .input(dust, Iron, 2)
                        .fluidInputs(Niobium.getFluid(72))
                        .fluidInputs(Molybdenum.getFluid(48))
                        .output(dust, Inconel, 10)
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
                        .output(dust, DepletedUraniumDioxide, 3)
                        .fluidOutputs(HydrofluoricAcid.getFluid(6000))
                        .buildAndRegister();

                FORMING_PRESS_RECIPES.recipeBuilder().duration(25).EUt(VA[EV])
                        .input(dust, LowEnrichedUraniumDioxide, 3)
                        .output(FUEL_PELLET_LEU235)
                        .buildAndRegister();

                ASSEMBLER_RECIPES.recipeBuilder().duration(800).EUt(VA[EV])
                        .input(plate, Zircaloy, 4)
                        .input(spring, Inconel, 1)
                        .input(round, StainlessSteel, 2)
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
                        .input(spring, Inconel, 1)
                        .input(round, StainlessSteel, 2)
                        .input(FUEL_PELLET_NATURAL_URANIUM, 16)
                        .output(FUEL_ROD_NATURAL_RUANIUM)
                        .buildAndRegister();

        // Fuel reprocessing

                // Fuel rod preparation and spent fuel dissolution

                SPENT_FUEL_POOL_RECIPES.recipeBuilder().duration(24000)
                        .input(FUEL_ROD_SPENT_URANIUM)
                        .output(FUEL_ROD_DECAYED_URANIUM)
                        .buildAndRegister();

                CUTTER_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                        .input(FUEL_ROD_DECAYED_URANIUM)
                        .output(FUEL_ROD_OPEN_URANIUM)
                        .buildAndRegister();

                LARGE_CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[EV])
                        .input(FUEL_ROD_OPEN_URANIUM)
                        .fluidInputs(NitricAcid.getFluid(64000))
                        .output(FUEL_ROD_CASING)
                        .output(round, StainlessSteel, 2)
                        .output(spring, Inconel, 1)
                        .fluidOutputs(SpentUraniumFuelSolution.getFluid(32000))
                        .fluidOutputs(NitrogenDioxide.getFluid(32000))
                        .buildAndRegister();

                MACERATOR_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                        .input(FUEL_ROD_OPEN_URANIUM)
                        .output(dust, Zircaloy, 4)
                        .buildAndRegister();

                // Purex Mixture

                LARGE_CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[EV])
                        .fluidInputs(Propene.getFluid(1000))
                        .fluidInputs(Water.getFluid(1000))
                        .fluidInputs(CarbonMonoxide.getFluid(2000))
                        .fluidOutputs(NButanol.getFluid(1000))
                        .fluidOutputs(CarbonDioxide.getFluid(1000))
                        .buildAndRegister();

                CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                        .input(dust, Phosphorus, 1)
                        .fluidInputs(Chlorine.getFluid(3000))
                        .fluidOutputs(PhosphorusTrichloride.getFluid(1000))
                        .buildAndRegister();

                CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                        .fluidInputs(PhosphorusTrichloride.getFluid(1000))
                        .fluidInputs(Oxygen.getFluid(1000))
                        .fluidOutputs(PhosphorylChloride.getFluid(1000))
                        .buildAndRegister();

                CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                        .fluidInputs(PhosphorylChloride.getFluid(1000))
                        .fluidInputs(NButanol.getFluid(3000))
                        .fluidOutputs(HydrochloricAcid.getFluid(3000))
                        .fluidOutputs(TributylPhosphate.getFluid(1000))
                        .buildAndRegister();
                
                MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV])
                        .fluidInputs(Kerosene.getFluid(750))
                        .fluidInputs(TributylPhosphate.getFluid(250))
                        .fluidOutputs(PurexMixture.gtFluid(1000))
                        .buildAndRegister();

                // Sodium nitrite production

                CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                        .input(dust, SodiumHydroxide, 6)
                        .fluidInputs(NitrogenDioxide.getFluid(1000))
                        .fluidInputs(NitricOxide.getFluid(1000))
                        .fluidInputs(Water.getFluid(1000))
                        .fluidOutputs(SodiumNitriteSolution.getFluid(2000))
                        .buildAndRegister();

                // Plutonium pretreatment

                CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                        .fluidInputs(SpentUraniumFuelSolution.getFluid(2000))
                        .fluidInputs(SodiumNitriteSolution.getFluid(75))
                        .fluidInputs(NitricAcid.getFluid(150))
                        .fluidOutputs(ConditionedUraniumFuelSolution.getFluid(2075))
                        .buildAndRegister();

                // U/Pu extraction
                 
                CENTRIFUGE_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                        .fluidInputs(ConditionedUraniumFuelSolution.getFluid(2075))
                        .fluidInputs(PurexMixture.getFluid(4000))
                        .fluidOutputs(TransplutoniumNitrateSolution.getFluid(2075))
                        .fluidOutputs(LoadedPurexMixture.getFluid(4000))
                        .buildAndRegister();

                // Uranyl ion stripping

                MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                        .fluidInputs(NitricAcid.getFluid(1000))
                        .fluidInputs(Water.getFluid(1000))
                        .fluidOutputs(DilutedNitricAcid.getFluid(2000))
                        .buildAndRegister();

                CENTRIFUGE_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                        .fluidInputs(LoadedPurexMixture.getFluid(4000))
                        .fluidInputs(DilutedNitricAcid.getFluid(3600))
                        .fluidOutputs(PlutoniumPurexMixture.getFluid(4000))
                        .fluidOutputs(ReactorUranylNitrateSolution.getFluid(3600))
                        .buildAndRegister();

                // Ferrous sulfamate, for Pu stripping

                CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                        .fluidInputs(Ammonia.getFluid(2000))
                        .fluidInputs(CarbonDioxide.getFluid(1000))
                        .fluidOutputs(UreaSolution.getFluid(1000))
                        .buildAndRegister();

                LARGE_CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[EV])
                        .fluidInputs(UreaSolution.getFluid(1000))
                        .fluidInputs(SulfurTrioxide.getFluid(1000))
                        .fluidInputs(SulfuricAcid.getFluid(1000))
                        .fluidOutputs(SulfamicAcidSolution.getFluid(1000))
                        .fluidOutputs(CarbonDioxide.getFluid(1000))
                        .buildAndRegister();

                CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                        .input(dust, Iron, 1)
                        .fluidInputs(Oxygen.getFluid(1000))
                        .fluidInputs(SulfamicAcidSolution.getFluid(1000))
                        .fluidOutputs(FerrousSulfamateSolution.getFluid(2000))
                        .buildAndRegister();

                // Pu stripping/reduction

                CENTRIFUGE_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                        .fluidInputs(PlutoniumPurexMixture.getFluid(4000)) // 0.075 mol Pu(NO3)4 * 2 TBP
                        .fluidInputs(FerrousSulfamateSolution.getFluid(150))
                        .fluidOutputs(PurexMixture.getFluid(4000))
                        .fluidOutputs(ReactorPlutoniumIIINitrateSolution.getFluid(150)) // 0.075 mol Pu(NO3)3, 0.075 mol FeNO3(NH2SO3)2
                        .buildAndRegister();

                // NH2SO3- + NO2- -> N2 + SO4 2- + H2O
                // 6Pu 3+ + 2NO2- + 8H+ -> 6 Pu 4+ + N2 + 4H2O

                CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                        .fluidInputs(ReactorPlutoniumIIINitrateSolution.getFluid(150))
                        .fluidInputs(SodiumNitriteSolution.getFluid(175))
                        .fluidInputs(NitricAcid.getFluid(100))
                        .fluidOutputs(ReactorPlutoniumIVNitrateSolution.getFluid(525)) 
                        .fluidOutputs(Nitrogen.getFluid(175))
                        .buildAndRegister();

                CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                        .inputs(dust, Sugar, 24)
                        .fluidInputs(NitricAcid.getFluid(2000)) 
                        .outputs(dust, OxalicAcid, 24)
                        .fluidOutputs(Water.getFluid(4000))
                        .fluidOutputs(NitrogenDioxide.getFluid(2000))
                        .buildAndRegister();

                CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                        .input(dust, OxalicAcid, 16)
                        .fluidInputs(ReactorPlutoniumIVNitrateSolution.getFluid(7000))
                        .output(dust, ReactorPlutoniumOxalate, 13)
                        .fluidOutputs(PlutoniumWaste.getFluid(7000))
                        .buildAndRegister();

                DISTILLATION_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                        .buildAndRegister();

                BLAST_RECIPES.recipeBuilder().duration(200).EUt(VA[EV]).blastFurnaceTemp(570)
                        .input(dust, ReactorPlutoniumOxalate, 13)
                        .fluidInputs(Oxygen.getFluid(2000))
                        .output(dust, ReactorPlutoniumDioxide, 9)
                        .fluidOutputs(CarbonDioxide.getFluid(4000))
                        .buildAndRegister();
                
        
    }
}

// REGISTER LIST
/*
- Thorium tetrafluoride
- Lithium fluoride
- Beryllium fluoride
*/
