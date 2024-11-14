package gregtech.loaders.recipe.chemistry;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.crushedPurified;
import static gregtech.api.unification.ore.OrePrefix.dust;

public class PlatGroupMetalsRecipes {

    public static void init() {
        // Primary Chain

        // Platinum Group Sludge Production
        CHEMICAL_RECIPES.recipeBuilder().duration(50).volts(VA[LV])
                .inputItem(crushedPurified, Chalcopyrite)
                .fluidInputs(NitricAcid.getFluid(100))
                .outputItem(dust, PlatinumGroupSludge, 2)
                .fluidOutputs(SulfuricCopperSolution.getFluid(1000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(50).volts(VA[LV])
                .inputItem(crushedPurified, Chalcocite)
                .fluidInputs(NitricAcid.getFluid(100))
                .outputItem(dust, PlatinumGroupSludge, 2)
                .fluidOutputs(SulfuricCopperSolution.getFluid(1000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(50).volts(VA[LV])
                .inputItem(crushedPurified, Bornite)
                .fluidInputs(NitricAcid.getFluid(100))
                .outputItem(dust, PlatinumGroupSludge, 2)
                .fluidOutputs(SulfuricCopperSolution.getFluid(1000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(50).volts(VA[LV])
                .inputItem(crushedPurified, Tetrahedrite)
                .fluidInputs(NitricAcid.getFluid(100))
                .outputItem(dust, PlatinumGroupSludge, 2)
                .fluidOutputs(SulfuricCopperSolution.getFluid(1000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(50).volts(VA[LV])
                .inputItem(crushedPurified, Pentlandite)
                .fluidInputs(NitricAcid.getFluid(100))
                .outputItem(dust, PlatinumGroupSludge, 2)
                .fluidOutputs(SulfuricNickelSolution.getFluid(1000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(50).volts(VA[LV])
                .inputItem(crushedPurified, Cooperite)
                .fluidInputs(NitricAcid.getFluid(100))
                .outputItem(dust, PlatinumGroupSludge, 4)
                .fluidOutputs(SulfuricNickelSolution.getFluid(1000))
                .buildAndRegister();

        // Aqua Regia
        // HNO3 + HCl -> [HNO3 + HCl]
        MIXER_RECIPES.recipeBuilder().duration(30).volts(VA[LV])
                .fluidInputs(NitricAcid.getFluid(1000))
                .fluidInputs(HydrochloricAcid.getFluid(2000))
                .fluidOutputs(AquaRegia.getFluid(3000))
                .buildAndRegister();

        // Platinum Group Sludge Break-Down
        //
        // MODIFY THIS RECIPE TO RE-BALANCE THE LINE
        //
        // Current Losses of Materials per recipe (update this if rebalanced):
        // H: Loses
        // N: Loses
        // O: Loses
        // Cl: Perfectly Conserved
        //
        // If modified, this is how much 1 of each product will change the above losses by:
        // Pt: 266L of Cl
        //
        // These numbers are not correct:
        // Pd: 200L of N, 600L of H
        // Ru/Rh: 667L of O
        // Ir/Os: 620L of O, 100L of H
        //
        // Can also modify the PtCl2 electrolyzer recipe to keep a perfect Cl ratio.
        //
        CENTRIFUGE_RECIPES.recipeBuilder().duration(500).volts(VA[HV])
                .inputItem(dust, PlatinumGroupSludge, 6)
                .fluidInputs(AquaRegia.getFluid(1200))
                .outputItem(dust, PlatinumRaw, 3) // PtCl2
                .outputItem(dust, PalladiumRaw, 3) // PdNH3
                .outputItem(dust, InertMetalMixture, 2) // RhRuO4
                .outputItem(dust, RarestMetalMixture) // IrOsO4(H2O)
                .outputItem(dust, PlatinumSludgeResidue, 2)
                .buildAndRegister();

        // PLATINUM

        ELECTROLYZER_RECIPES.recipeBuilder().duration(100).volts(VA[MV])
                .inputItem(dust, PlatinumRaw, 3)
                .outputItem(dust, Platinum)
                .fluidOutputs(Chlorine.getFluid(800))
                .buildAndRegister();

        // PALLADIUM

        CHEMICAL_RECIPES.recipeBuilder().duration(200).volts(VA[MV])
                .inputItem(dust, PalladiumRaw, 5)
                .fluidInputs(HydrochloricAcid.getFluid(1000))
                .outputItem(dust, Palladium)
                .outputItem(dust, AmmoniumChloride, 2)
                .buildAndRegister();

        // RHODIUM / RUTHENIUM

        CHEMICAL_RECIPES.recipeBuilder().duration(450).volts(VA[EV])
                .inputItem(dust, InertMetalMixture, 6)
                .fluidInputs(SulfuricAcid.getFluid(1500))
                .fluidOutputs(RhodiumSulfate.getFluid(500))
                .outputItem(dust, RutheniumTetroxide, 5)
                .fluidOutputs(Hydrogen.getFluid(3000))
                .buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder().duration(100).volts(VA[MV])
                .fluidInputs(RhodiumSulfate.getFluid(1000))
                .outputItem(dust, Rhodium, 2)
                .fluidOutputs(SulfurTrioxide.getFluid(3000))
                .fluidOutputs(Oxygen.getFluid(3000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(200).volts(VA[MV])
                .inputItem(dust, RutheniumTetroxide, 5)
                .inputItem(dust, Carbon, 2)
                .outputItem(dust, Ruthenium)
                .fluidOutputs(CarbonDioxide.getFluid(2000))
                .buildAndRegister();

        // OSMIUM / IRIDIUM

        LARGE_CHEMICAL_RECIPES.recipeBuilder().duration(400).volts(VA[IV])
                .inputItem(dust, RarestMetalMixture, 7)
                .fluidInputs(HydrochloricAcid.getFluid(4000))
                .outputItem(dust, IridiumMetalResidue, 5)
                .fluidOutputs(AcidicOsmiumSolution.getFluid(2000))
                .fluidOutputs(Hydrogen.getFluid(3000))
                .buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder().duration(400).volts(VA[MV])
                .fluidInputs(AcidicOsmiumSolution.getFluid(2000))
                .outputItem(dust, OsmiumTetroxide, 5)
                .fluidOutputs(HydrochloricAcid.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(200).volts(VA[LV])
                .inputItem(dust, OsmiumTetroxide, 5)
                .fluidInputs(Hydrogen.getFluid(8000))
                .outputItem(dust, Osmium)
                .fluidOutputs(Water.getFluid(4000))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(200).volts(VA[MV])
                .inputItem(dust, IridiumMetalResidue, 5)
                .outputItem(dust, IridiumChloride, 4)
                .outputItem(dust, PlatinumSludgeResidue)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(100).volts(VA[LV])
                .inputItem(dust, IridiumChloride, 4)
                .fluidInputs(Hydrogen.getFluid(3000))
                .outputItem(dust, Iridium)
                .fluidOutputs(HydrochloricAcid.getFluid(3000))
                .buildAndRegister();
    }
}
