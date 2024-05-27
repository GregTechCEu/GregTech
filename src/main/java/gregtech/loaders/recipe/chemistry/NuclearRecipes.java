package gregtech.loaders.recipe.chemistry;

import gregtech.common.items.MetaItems;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class NuclearRecipes {

    public static void init() {
        // Thorium Fuel
        /*
         * CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
         * .input(dust, Thorium, 1)
         * .fluidInputs(Fluorine.getFluid(4000))
         * .output(dust, ThoriumTetrafluoride, 5)
         * .buildAndRegister();
         * 
         * CHEMICAL_RECIPES.recipeBuilder().duration(100).EUt(VA[LV])
         * .input(dust, Lithium, 1)
         * .fluidInputs(Fluorine.getFluid(1000))
         * .output(dust, LithiumFluoride, 2)
         * .buildAndRegister();
         * 
         * CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
         * .input(dust, Beryllium, 1)
         * .fluidInputs(Fluorine.getFluid(2000))
         * .output(dust, BerylliumFluoride, 3)
         * .buildAndRegister();
         * 
         * BLAST_RECIPES.recipeBuilder().duration(200).EUt(VA[EV]).blastFurnaceTemp(1000)
         * .input(dust, ThoriumTetrafluoride, 5)
         * .input(dust, LithiumFluoride, 4)
         * .input(dust, BerylliumFluoride, 3)
         * .fluidOutputs(ThoriumFLiBe.getFluid(4000))
         * .buildAndRegister();
         */

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

        GAS_CENTRIFUGE_RECIPES.recipeBuilder().duration(160).EUt(VA[HV])
                .fluidInputs(LowEnrichedUraniumHexafluoride.getFluid(1000))
                .fluidOutputs(HighEnrichedUraniumHexafluoride.getFluid(100))
                .fluidOutputs(DepletedUraniumHexafluoride.getFluid(900))
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).EUt(VA[HV])
                .input(dust, Plutonium239Dioxide, 1)
                .input(dust, Uraninite, 20)
                .circuitMeta(1)
                .output(dust, LowGradeMOX, 1)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).EUt(VA[HV])
                .input(dust, Plutonium239Dioxide, 1)
                .input(dust, Uraninite, 5)
                .circuitMeta(2)
                .output(dust, HighGradeMOX, 1)
                .buildAndRegister();

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
                .input(dust, Chrome, 2)
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

        // U/Pu extraction

        ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(VA[LV])
                .input(ring, Titanium, 2)
                .input(stick, Titanium, 16)
                .output(MetaItems.ANODE_BASKET)
                .buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder().duration(800).EUt(VA[EV])
                .notConsumable(MetaItems.ANODE_BASKET)
                .notConsumable(Salt.getFluid(1000))
                .input(fuelPelletDepleted, LEU235)
                .output(dustSpentFuel, LEU235)
                .output(dustBredFuel, LEU235)
                .chancedOutput(dustFissionByproduct, LEU235, 266, 0)
                .buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder().duration(800).EUt(VA[EV])
                .notConsumable(MetaItems.ANODE_BASKET)
                .notConsumable(Salt.getFluid(1000))
                .input(fuelPelletDepleted, HEU235)
                .output(dustSpentFuel, HEU235)
                .output(dustBredFuel, HEU235)
                .chancedOutput(dustFissionByproduct, LEU235, 266, 0)
                .buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder().duration(800).EUt(VA[EV])
                .notConsumable(MetaItems.ANODE_BASKET)
                .notConsumable(Salt.getFluid(1000))
                .input(fuelPelletDepleted, LowGradeMOX)
                .output(dustSpentFuel, LowGradeMOX)
                .output(dustBredFuel, LowGradeMOX)
                .chancedOutput(dustFissionByproduct, LowGradeMOX, 266, 0)
                .buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder().duration(800).EUt(VA[EV])
                .notConsumable(MetaItems.ANODE_BASKET)
                .notConsumable(Salt.getFluid(1000))
                .input(fuelPelletDepleted, HighGradeMOX)
                .output(dustSpentFuel, HighGradeMOX)
                .output(dustBredFuel, HighGradeMOX)
                .chancedOutput(dustFissionByproduct, HighGradeMOX, 266, 0)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(dustSpentFuel, LEU235, 1)
                .fluidInputs(HydrofluoricAcid.getFluid(4000))
                .fluidInputs(Fluorine.getFluid(2000))
                .fluidOutputs(UraniumHexafluoride.getFluid(1000))
                .fluidOutputs(Water.getFluid(2000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(dustSpentFuel, HEU235, 1)
                .fluidInputs(HydrofluoricAcid.getFluid(4000))
                .fluidInputs(Fluorine.getFluid(2000))
                .fluidOutputs(LowEnrichedUraniumHexafluoride.getFluid(1000))
                .fluidOutputs(Water.getFluid(2000))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(dustBredFuel, LEU235)
                .chancedOutput(dust, Plutonium239, 47, 0)
                .chancedOutput(dust, Plutonium240, 22, 0)
                .chancedOutput(dust, Plutonium241, 14, 0)
                .chancedOutput(dust, Neptunium239, 3, 0)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(dustBredFuel, HEU235)
                .chancedOutput(dust, Plutonium239, 47, 0)
                .chancedOutput(dust, Plutonium240, 22, 0)
                .chancedOutput(dust, Plutonium241, 14, 0)
                .chancedOutput(dust, Neptunium239, 3, 0)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(dustFissionByproduct, LEU235)
                .chancedOutput(dust, Zirconium, 1040, 0)
                .chancedOutput(dust, Technetium, 1190, 0)
                .chancedOutput(dust, Caesium, 1100, 0)
                .chancedOutput(dust, Neodymium, 1110, 0)
                .chancedOutput(dust, Ruthenium, 690, 0)
                .chancedOutput(dust, Palladium, 370, 0)
                .chancedFluidOutput(Xenon.getFluid(1000), 1270, 0)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(dustFissionByproduct, LowGradeMOX)
                .chancedOutput(dust, Zirconium, 740, 0)
                .chancedOutput(dust, Technetium, 1140, 0)
                .chancedOutput(dust, Caesium, 1140, 0)
                .chancedOutput(dust, Neodymium, 980, 0)
                .chancedOutput(dust, Ruthenium, 890, 0)
                .chancedOutput(dust, Palladium, 730, 0)
                .chancedFluidOutput(Xenon.getFluid(1000), 1330, 0)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(dustFissionByproduct, HighGradeMOX)
                .chancedOutput(dust, Zirconium, 740, 0)
                .chancedOutput(dust, Molybdenum, 900, 0)
                .chancedOutput(dust, Caesium, 1140, 0)
                .chancedOutput(dust, Neodymium, 980, 0)
                .chancedOutput(dust, Ruthenium, 890, 0)
                .chancedOutput(dust, Palladium, 730, 0)
                .chancedFluidOutput(Xenon.getFluid(1000), 1330, 0)
                .buildAndRegister();
    }
}
