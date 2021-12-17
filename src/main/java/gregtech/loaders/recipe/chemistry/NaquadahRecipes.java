package gregtech.loaders.recipe.chemistry;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

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

        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[ULV]).duration(60)
                .input(dust, Antimony, 2)
                .fluidInputs(Oxygen.getFluid(3000))
                .output(dust, AntimonyTrioxide, 5)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[LV]).duration(60)
                .input(dust, AntimonyTrioxide, 5)
                .fluidInputs(HydrofluoricAcid.getFluid(6000))
                .output(dust, AntimonyTrifluoride, 8)
                .fluidOutputs(Water.getFluid(3000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[HV]).duration(300)
                .input(dust, AntimonyTrifluoride, 4)
                .fluidInputs(HydrofluoricAcid.getFluid(4000))
                .fluidOutputs(FluoroantimonicAcid.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(2000))
                .buildAndRegister();


        // STARTING POINT

        LARGE_CHEMICAL_RECIPES.recipeBuilder().EUt(VA[LuV]).duration(600)
                .fluidInputs(FluoroantimonicAcid.getFluid(1000))
                .input(dust, Naquadah, 6)
                .fluidOutputs(ImpureEnrichedNaquadahSolution.getFluid(2000))
                .fluidOutputs(ImpureNaquadriaSolution.getFluid(2000))
                .output(dust, TitaniumTrifluoride, 4)
                .buildAndRegister();


        // ENRICHED NAQUADAH PROCESS

        CENTRIFUGE_RECIPES.recipeBuilder().EUt(VA[EV]).duration(400)
                .fluidInputs(ImpureEnrichedNaquadahSolution.getFluid(2000))
                .output(dust, TriniumSulfide)
                .output(dust, AntimonyTrifluoride, 2)
                .fluidOutputs(EnrichedNaquadahSolution.getFluid(1000))
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().EUt(VA[HV]).duration(100)
                .fluidInputs(EnrichedNaquadahSolution.getFluid(1000))
                .fluidInputs(SulfuricAcid.getFluid(2000))
                .fluidOutputs(AcidicEnrichedNaquadahSolution.getFluid(3000))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().EUt(VA[HV]).duration(100)
                .fluidInputs(AcidicEnrichedNaquadahSolution.getFluid(3000))
                .fluidOutputs(EnrichedNaquadahWaste.getFluid(2000))
                .fluidOutputs(Fluorine.getFluid(500))
                .output(dust, EnrichedNaquadahSulfate, 6) // Nq+SO4
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().EUt(VA[IV]).duration(500).blastFurnaceTemp(7000)
                .input(dust, EnrichedNaquadahSulfate, 6)
                .fluidInputs(Hydrogen.getFluid(2000))
                .output(ingotHot, NaquadahEnriched)
                .fluidOutputs(SulfuricAcid.getFluid(1000))
                .buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder().EUt(VA[HV]).duration(300)
                .fluidInputs(EnrichedNaquadahWaste.getFluid(2000))
                .output(dustSmall, BariumSulfide, 2)
                .fluidOutputs(SulfuricAcid.getFluid(500))
                .fluidOutputs(EnrichedNaquadahSolution.getFluid(250))
                .fluidOutputs(NaquadriaSolution.getFluid(100))
                .buildAndRegister();


        // NAQUADRIA PROCESS

        CENTRIFUGE_RECIPES.recipeBuilder().EUt(VA[EV]).duration(400)
                .fluidInputs(ImpureNaquadriaSolution.getFluid(2000))
                .output(dust, IndiumPhosphide)
                .output(dust, AntimonyTrifluoride, 2)
                .fluidOutputs(NaquadriaSolution.getFluid(1000))
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().EUt(VA[HV]).duration(100)
                .fluidInputs(NaquadriaSolution.getFluid(1000))
                .fluidInputs(SulfuricAcid.getFluid(2000))
                .fluidOutputs(AcidicNaquadriaSolution.getFluid(3000))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().EUt(VA[HV]).duration(100)
                .fluidInputs(AcidicNaquadriaSolution.getFluid(3000))
                .fluidOutputs(NaquadriaWaste.getFluid(2000))
                .fluidOutputs(Fluorine.getFluid(500))
                .output(dust, NaquadriaSulfate, 6)
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().EUt(VA[ZPM]).duration(600).blastFurnaceTemp(9000)
                .input(dust, NaquadriaSulfate, 6)
                .fluidInputs(Hydrogen.getFluid(2000))
                .output(ingotHot, Naquadria)
                .fluidOutputs(SulfuricAcid.getFluid(1000))
                .buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder().EUt(VA[HV]).duration(300)
                .fluidInputs(NaquadriaWaste.getFluid(2000))
                .output(dustSmall, GalliumSulfide, 2)
                .fluidOutputs(SulfuricAcid.getFluid(500))
                .fluidOutputs(NaquadriaSolution.getFluid(250))
                .fluidOutputs(EnrichedNaquadahSolution.getFluid(100))
                .buildAndRegister();


        // TRINIUM

        BLAST_RECIPES.recipeBuilder().duration(750).EUt(VA[LuV]).blastFurnaceTemp(Trinium.getBlastTemperature())
                .input(dust, TriniumSulfide, 2)
                .input(dust, Zinc)
                .output(ingotHot, Trinium)
                .output(dust, ZincSulfide, 2)
                .buildAndRegister();


        // BYPRODUCT PROCESSING

        // Titanium Trifluoride
        BLAST_RECIPES.recipeBuilder().EUt(VA[HV]).duration(900).blastFurnaceTemp(1941)
                .input(dust, TitaniumTrifluoride, 4)
                .fluidInputs(Hydrogen.getFluid(3000))
                .output(ingotHot, Titanium)
                .fluidOutputs(HydrofluoricAcid.getFluid(3000))
                .buildAndRegister();

        // Indium Phosphide
        CHEMICAL_RECIPES.recipeBuilder().duration(30).EUt(VA[ULV])
                .input(dust, IndiumPhosphide, 2)
                .input(dust, Calcium)
                .output(dust, Indium)
                .output(dust, CalciumPhosphide, 2)
                .buildAndRegister();
    }
}
