package gregtech.loaders.recipe.chemistry;

import gregtech.common.items.MetaItems;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class NuclearRecipes {

    public static void init() {
        // Boron carbide for spent fuel racks

        CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(dust, Boron, 2)
                .fluidInputs(Oxygen.getFluid(3000))
                .output(dust, BoronTrioxide, 5)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(400).EUt(VA[MV])
                .input(dust, BoronTrioxide, 2)
                .input(dust, Carbon, 7)
                .output(dust, BoronCarbide, 5)
                .fluidOutputs(CarbonMonoxide.getFluid(6000))
                .buildAndRegister();

        // Uranium enrichment

        CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(dust, Uraninite, 3)
                .fluidInputs(HydrofluoricAcid.getFluid(4000))
                .fluidInputs(Fluorine.getFluid(2000))
                .fluidOutputs(UraniumHexafluoride.getFluid(1000))
                .fluidOutputs(Water.getFluid(2000))
                .buildAndRegister();

        GAS_CENTRIFUGE_RECIPES.recipeBuilder().duration(800).EUt(VA[HV])
                .fluidInputs(UraniumHexafluoride.getFluid(1000))
                .fluidOutputs(LowEnrichedUraniumHexafluoride.getFluid(100))
                .fluidOutputs(DepletedUraniumHexafluoride.getFluid(900))
                .buildAndRegister();

        GAS_CENTRIFUGE_RECIPES.recipeBuilder().duration(800).EUt(VA[HV])
                .fluidInputs(LowEnrichedUraniumHexafluoride.getFluid(1000))
                .fluidOutputs(HighEnrichedUraniumHexafluoride.getFluid(100))
                .fluidOutputs(DepletedUraniumHexafluoride.getFluid(900))
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).EUt(VA[HV])
                .input(dust, FissilePlutoniumDioxide, 1)
                .input(dust, Uraninite, 20)
                .circuitMeta(1)
                .output(dust, LowGradeMOX, 21)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).EUt(VA[HV])
                .input(dust, FissilePlutoniumDioxide, 1)
                .input(dust, Uraninite, 5)
                .circuitMeta(2)
                .output(dust, HighGradeMOX, 6)
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
                .input(dust, Tin, 2)
                .input(dust, Chrome, 1)
                .output(dust, Zircaloy, 19)
                .buildAndRegister();

        // Inconel 718
        MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV])
                .input(dust, Nickel, 5)
                .input(dust, Chrome, 2)
                .input(dust, Iron, 2)
                .input(dust, Niobium)
                .input(dust, Molybdenum)
                .output(dust, Inconel, 10)
                .buildAndRegister();

        // LEU-235 Dioxide
        CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .fluidInputs(HighEnrichedUraniumHexafluoride.getFluid(1000))
                .fluidInputs(Water.getFluid(2000))
                .fluidInputs(Hydrogen.getFluid(2000))
                .output(dust, HighEnrichedUraniumDioxide, 1)
                .fluidOutputs(HydrofluoricAcid.getFluid(6000))
                .buildAndRegister();

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

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(dust, HighEnrichedUraniumDioxide, 1)
                .input(dust, DepletedUraniumDioxide, 5)
                .output(dust, HEU235, 6)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(dust, HighEnrichedUraniumDioxide, 1)
                .input(dust, DepletedUraniumDioxide, 20)
                .output(dust, LEU235, 21)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(40).EUt(VA[ULV])
                .input(dust, Plutonium239)
                .fluidInputs(Oxygen.getFluid(2000))
                .output(dust, FissilePlutoniumDioxide, 3)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(40).EUt(VA[ULV])
                .input(dust, Plutonium241)
                .fluidInputs(Oxygen.getFluid(2000))
                .output(dust, FissilePlutoniumDioxide, 3)
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
                .chancedOutput(dustFissionByproduct, HEU235, 301, 0)
                .buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder().duration(800).EUt(VA[EV])
                .notConsumable(MetaItems.ANODE_BASKET)
                .notConsumable(Salt.getFluid(1000))
                .input(fuelPelletDepleted, LowGradeMOX)
                .output(dustSpentFuel, LowGradeMOX)
                .output(dustBredFuel, LowGradeMOX)
                .chancedOutput(dustFissionByproduct, LowGradeMOX, 229, 0)
                .buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder().duration(800).EUt(VA[EV])
                .notConsumable(MetaItems.ANODE_BASKET)
                .notConsumable(Salt.getFluid(1000))
                .input(fuelPelletDepleted, HighGradeMOX)
                .output(dustSpentFuel, HighGradeMOX)
                .output(dustBredFuel, HighGradeMOX)
                .chancedOutput(dustFissionByproduct, HighGradeMOX, 443, 0)
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

        CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(dustSpentFuel, LowGradeMOX, 1)
                .fluidInputs(HydrofluoricAcid.getFluid(4000))
                .fluidInputs(Fluorine.getFluid(2000))
                .fluidOutputs(DepletedUraniumHexafluoride.getFluid(1000))
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
                .input(dustBredFuel, LowGradeMOX)
                .chancedOutput(dust, Plutonium240, 33, 0)
                .chancedOutput(dust, Plutonium241, 1, 0)
                .chancedOutput(dust, Neptunium239, 3, 0)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(dustBredFuel, HighGradeMOX)
                .chancedOutput(dust, Plutonium240, 724, 0)
                .chancedOutput(dust, Plutonium241, 192, 0)
                .chancedOutput(dust, Plutonium242, 59, 0)
                .chancedOutput(dust, Neptunium239, 3, 0)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(dustFissionByproduct, LEU235)
                .chancedOutput(dust, Molybdenum, 800, 0)
                .chancedOutput(dust, Technetium, 755, 0)
                .chancedOutput(dust, Caesium, 2390, 0)
                .chancedOutput(dust, Barium, 1751, 0)
                .chancedOutput(dust, Cerium, 778, 0)
                .chancedOutput(dust, Praseodymium, 722, 0)
                .chancedFluidOutput(Xenon.getFluid(1000), 881, 0)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(dustFissionByproduct, HEU235)
                .chancedOutput(dust, Strontium, 706, 0)
                .chancedOutput(dust, Technetium, 755, 0)
                .chancedOutput(dust, Caesium, 2390, 0)
                .chancedOutput(dust, Barium, 1751, 0)
                .chancedOutput(dust, Cerium, 777, 0)
                .chancedOutput(dust, Praseodymium, 721, 0)
                .chancedFluidOutput(Xenon.getFluid(1000), 881, 0)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(dustFissionByproduct, LowGradeMOX)
                .chancedOutput(dust, Technetium, 750, 0)
                .chancedOutput(dust, Caesium, 2490, 0)
                .chancedOutput(dust, Neodymium, 519, 0)
                .chancedOutput(dust, Praseodymium, 654, 0)
                .chancedOutput(dust, Barium, 1874, 0)
                .chancedOutput(dust, Palladium, 463, 0)
                .chancedFluidOutput(Xenon.getFluid(1000), 1019, 0)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(dustFissionByproduct, HighGradeMOX)
                .chancedOutput(dust, Technetium, 744, 0)
                .chancedOutput(dust, Caesium, 2497, 0)
                .chancedOutput(dust, Barium, 1893, 0)
                .chancedOutput(dust, Cerium, 693, 0)
                .chancedOutput(dust, Praseodymium, 654, 0)
                .chancedOutput(dust, Promethium, 265, 0)
                .chancedFluidOutput(Xenon.getFluid(1000), 1025, 0)
                .buildAndRegister();
    }
}
