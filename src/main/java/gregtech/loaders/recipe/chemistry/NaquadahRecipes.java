package gregtech.loaders.recipe.chemistry;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;
import static gregtech.api.unification.ore.OrePrefix.ingotHot;

public class NaquadahRecipes {

    // Rough ratio of Naquadah Dust breakdown from this process:
    //
    // 6 NAQUADAH DUST:
    // |> 1 Enriched Naquadah
    // |> 1 Naquadria
    // |> 1 Titanium
    // |> 1 Sulfur
    // |> 0.5 Indium
    // |> 0.5 Trinium
    // |> 0.5 Phosphorus
    // |> 0.25 Gallium
    // |> 0.25 Barium

    public static void init() {
        // FLUOROANTIMONIC ACID

        CHEMICAL_RECIPES.recipeBuilder().volts(VA[ULV]).duration(60)
                .inputItem(dust, Antimony, 2)
                .fluidInputs(Oxygen.getFluid(3000))
                .outputItem(dust, AntimonyTrioxide, 5)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().volts(VA[LV]).duration(60)
                .inputItem(dust, AntimonyTrioxide, 5)
                .fluidInputs(HydrofluoricAcid.getFluid(6000))
                .outputItem(dust, AntimonyTrifluoride, 8)
                .fluidOutputs(Water.getFluid(3000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().volts(VA[HV]).duration(300)
                .inputItem(dust, AntimonyTrifluoride, 4)
                .fluidInputs(HydrofluoricAcid.getFluid(4000))
                .fluidOutputs(FluoroantimonicAcid.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(2000))
                .buildAndRegister();

        // STARTING POINT

        LARGE_CHEMICAL_RECIPES.recipeBuilder().volts(VA[LuV]).duration(600)
                .fluidInputs(FluoroantimonicAcid.getFluid(1000))
                .inputItem(dust, Naquadah, 6)
                .fluidOutputs(ImpureEnrichedNaquadahSolution.getFluid(2000))
                .fluidOutputs(ImpureNaquadriaSolution.getFluid(2000))
                .outputItem(dust, TitaniumTrifluoride, 4)
                .buildAndRegister();

        // ENRICHED NAQUADAH PROCESS

        CENTRIFUGE_RECIPES.recipeBuilder().volts(VA[EV]).duration(400)
                .fluidInputs(ImpureEnrichedNaquadahSolution.getFluid(2000))
                .outputItem(dust, TriniumSulfide)
                .outputItem(dust, AntimonyTrifluoride, 2)
                .fluidOutputs(EnrichedNaquadahSolution.getFluid(1000))
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().volts(VA[HV]).duration(100)
                .fluidInputs(EnrichedNaquadahSolution.getFluid(1000))
                .fluidInputs(SulfuricAcid.getFluid(2000))
                .fluidOutputs(AcidicEnrichedNaquadahSolution.getFluid(3000))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().volts(VA[HV]).duration(100)
                .fluidInputs(AcidicEnrichedNaquadahSolution.getFluid(3000))
                .fluidOutputs(EnrichedNaquadahWaste.getFluid(2000))
                .fluidOutputs(Fluorine.getFluid(250))
                .outputItem(dust, EnrichedNaquadahSulfate, 6) // Nq+SO4
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().volts(VA[IV]).duration(500).blastFurnaceTemp(7000)
                .inputItem(dust, EnrichedNaquadahSulfate, 6)
                .fluidInputs(Hydrogen.getFluid(2000))
                .outputItem(ingotHot, NaquadahEnriched)
                .fluidOutputs(SulfuricAcid.getFluid(1000))
                .buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder().volts(VA[HV]).duration(300)
                .fluidInputs(EnrichedNaquadahWaste.getFluid(2000)).outputItemRoll(dust, BariumSulfide, 5000, 0)
                .fluidOutputs(SulfuricAcid.getFluid(500))
                .fluidOutputs(EnrichedNaquadahSolution.getFluid(350))
                .fluidOutputs(NaquadriaSolution.getFluid(150))
                .buildAndRegister();

        // NAQUADRIA PROCESS

        CENTRIFUGE_RECIPES.recipeBuilder().volts(VA[EV]).duration(400)
                .fluidInputs(ImpureNaquadriaSolution.getFluid(2000))
                .outputItem(dust, IndiumPhosphide)
                .outputItem(dust, AntimonyTrifluoride, 2)
                .fluidOutputs(NaquadriaSolution.getFluid(1000))
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().volts(VA[HV]).duration(100)
                .fluidInputs(NaquadriaSolution.getFluid(1000))
                .fluidInputs(SulfuricAcid.getFluid(2000))
                .fluidOutputs(AcidicNaquadriaSolution.getFluid(3000))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().volts(VA[HV]).duration(100)
                .fluidInputs(AcidicNaquadriaSolution.getFluid(3000))
                .fluidOutputs(NaquadriaWaste.getFluid(2000))
                .fluidOutputs(Fluorine.getFluid(250))
                .outputItem(dust, NaquadriaSulfate, 6)
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().volts(VA[ZPM]).duration(600).blastFurnaceTemp(9000)
                .inputItem(dust, NaquadriaSulfate, 6)
                .fluidInputs(Hydrogen.getFluid(2000))
                .outputItem(ingotHot, Naquadria)
                .fluidOutputs(SulfuricAcid.getFluid(1000))
                .buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder().volts(VA[HV]).duration(300)
                .fluidInputs(NaquadriaWaste.getFluid(2000)).outputItemRoll(dust, GalliumSulfide, 5000, 0)
                .fluidOutputs(SulfuricAcid.getFluid(500))
                .fluidOutputs(NaquadriaSolution.getFluid(350))
                .fluidOutputs(EnrichedNaquadahSolution.getFluid(150))
                .buildAndRegister();

        // TRINIUM

        BLAST_RECIPES.recipeBuilder().duration(750).volts(VA[LuV]).blastFurnaceTemp(Trinium.getBlastTemperature())
                .inputItem(dust, TriniumSulfide, 2)
                .inputItem(dust, Zinc)
                .outputItem(ingotHot, Trinium)
                .outputItem(dust, ZincSulfide, 2)
                .buildAndRegister();

        // BYPRODUCT PROCESSING

        // Titanium Trifluoride
        BLAST_RECIPES.recipeBuilder().volts(VA[HV]).duration(900).blastFurnaceTemp(1941)
                .inputItem(dust, TitaniumTrifluoride, 4)
                .fluidInputs(Hydrogen.getFluid(3000))
                .outputItem(ingotHot, Titanium)
                .fluidOutputs(HydrofluoricAcid.getFluid(3000))
                .buildAndRegister();

        // Indium Phosphide
        CHEMICAL_RECIPES.recipeBuilder().duration(30).volts(VA[ULV])
                .inputItem(dust, IndiumPhosphide, 2)
                .inputItem(dust, Calcium)
                .outputItem(dust, Indium)
                .outputItem(dust, CalciumPhosphide, 2)
                .buildAndRegister();
    }
}
