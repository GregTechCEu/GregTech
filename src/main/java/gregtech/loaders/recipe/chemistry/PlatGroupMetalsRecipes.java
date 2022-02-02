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
        CHEMICAL_RECIPES.recipeBuilder().duration(50).EUt(VA[LV])
                .input(crushedPurified, Chalcopyrite)
                .fluidInputs(NitricAcid.getFluid(100))
                .output(dust, PlatinumGroupSludge, 2)
                .fluidOutputs(SulfuricCopperSolution.getFluid(1000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(50).EUt(VA[LV])
                .input(crushedPurified, Chalcocite)
                .fluidInputs(NitricAcid.getFluid(100))
                .output(dust, PlatinumGroupSludge, 2)
                .fluidOutputs(SulfuricCopperSolution.getFluid(1000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(50).EUt(VA[LV])
                .input(crushedPurified, Bornite)
                .fluidInputs(NitricAcid.getFluid(100))
                .output(dust, PlatinumGroupSludge, 2)
                .fluidOutputs(SulfuricCopperSolution.getFluid(1000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(50).EUt(VA[LV])
                .input(crushedPurified, Tetrahedrite)
                .fluidInputs(NitricAcid.getFluid(100))
                .output(dust, PlatinumGroupSludge, 2)
                .fluidOutputs(SulfuricCopperSolution.getFluid(1000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(50).EUt(VA[LV])
                .input(crushedPurified, Pentlandite)
                .fluidInputs(NitricAcid.getFluid(100))
                .output(dust, PlatinumGroupSludge, 2)
                .fluidOutputs(SulfuricNickelSolution.getFluid(1000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(50).EUt(VA[LV])
                .input(crushedPurified, Cooperite)
                .fluidInputs(NitricAcid.getFluid(100))
                .output(dust, PlatinumGroupSludge, 4)
                .fluidOutputs(SulfuricNickelSolution.getFluid(1000))
                .buildAndRegister();

        // Aqua Regia
        // HNO3 + HCl -> [HNO3 + HCl]
        MIXER_RECIPES.recipeBuilder().duration(30).EUt(VA[LV])
                .fluidInputs(NitricAcid.getFluid(1000))
                .fluidInputs(HydrochloricAcid.getFluid(2000))
                .fluidOutputs(AquaRegia.getFluid(3000))
                .buildAndRegister();

        // Platinum Group Sludge Break-Down
        //
        // MODIFY THIS RECIPE TO RE-BALANCE THE LINE
        //
        // Current Losses of Materials per recipe (update this if rebalanced):
        // H:  Loses
        // N:  Loses
        // O:  Loses
        // Cl: Perfectly Conserved
        //
        // If modified, this is how much 1 of each product will change the above losses by:
        // Pt:    266L of Cl
        //
        // These numbers are not correct:
        // Pd:    200L of N, 600L of H
        // Ru/Rh: 667L of O
        // Ir/Os: 620L of O, 100L of H
        //
        // Can also modify the PtCl2 electrolyzer recipe to keep a perfect Cl ratio.
        //
        CENTRIFUGE_RECIPES.recipeBuilder().duration(500).EUt(VA[HV])
                .input(dust, PlatinumGroupSludge, 6)
                .fluidInputs(AquaRegia.getFluid(1200))
                .output(dust, PlatinumRaw, 3) // PtCl2
                .output(dust, PalladiumRaw, 3) // PdNH3
                .output(dust, InertMetalMixture, 2) // RhRuO4
                .output(dust, RarestMetalMixture) // IrOsO4(H2O)
                .output(dust, PlatinumSludgeResidue, 2)
                .buildAndRegister();


        // PLATINUM

        ELECTROLYZER_RECIPES.recipeBuilder().duration(100).EUt(VA[MV])
                .input(dust, PlatinumRaw, 3)
                .output(dust, Platinum)
                .fluidOutputs(Chlorine.getFluid(800))
                .buildAndRegister();


        // PALLADIUM

        CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[MV])
                .input(dust, PalladiumRaw, 5)
                .fluidInputs(HydrochloricAcid.getFluid(1000))
                .output(dust, Palladium)
                .output(dust, AmmoniumChloride, 2)
                .buildAndRegister();


        // RHODIUM / RUTHENIUM

        CHEMICAL_RECIPES.recipeBuilder().duration(450).EUt(VA[EV])
                .input(dust, InertMetalMixture, 6)
                .fluidInputs(SulfuricAcid.getFluid(1500))
                .fluidOutputs(RhodiumSulfate.getFluid(500))
                .output(dust, RutheniumTetroxide, 5)
                .fluidOutputs(Hydrogen.getFluid(3000))
                .buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder().duration(100).EUt(VA[MV])
                .fluidInputs(RhodiumSulfate.getFluid(1000))
                .output(dust, Rhodium, 2)
                .fluidOutputs(SulfurTrioxide.getFluid(3000))
                .fluidOutputs(Oxygen.getFluid(3000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[MV])
                .input(dust, RutheniumTetroxide, 5)
                .input(dust, Carbon, 2)
                .output(dust, Ruthenium)
                .fluidOutputs(CarbonDioxide.getFluid(2000))
                .buildAndRegister();


        // OSMIUM / IRIDIUM

        LARGE_CHEMICAL_RECIPES.recipeBuilder().duration(400).EUt(VA[IV])
                .input(dust, RarestMetalMixture, 7)
                .fluidInputs(HydrochloricAcid.getFluid(4000))
                .output(dust, IridiumMetalResidue, 5)
                .fluidOutputs(AcidicOsmiumSolution.getFluid(2000))
                .fluidOutputs(Hydrogen.getFluid(3000))
                .buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder().duration(400).EUt(VA[MV])
                .fluidInputs(AcidicOsmiumSolution.getFluid(2000))
                .output(dust, OsmiumTetroxide, 5)
                .fluidOutputs(HydrochloricAcid.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(dust, OsmiumTetroxide, 5)
                .fluidInputs(Hydrogen.getFluid(8000))
                .output(dust, Osmium)
                .fluidOutputs(Water.getFluid(4000))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(200).EUt(VA[MV])
                .input(dust, IridiumMetalResidue, 5)
                .output(dust, IridiumChloride, 4)
                .output(dust, PlatinumSludgeResidue)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(100).EUt(VA[LV])
                .input(dust, IridiumChloride, 4)
                .fluidInputs(Hydrogen.getFluid(3000))
                .output(dust, Iridium)
                .fluidOutputs(HydrochloricAcid.getFluid(3000))
                .buildAndRegister();
    }
}
