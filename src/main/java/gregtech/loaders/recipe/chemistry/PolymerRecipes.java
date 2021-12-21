package gregtech.loaders.recipe.chemistry;

import gregtech.api.recipes.ingredients.IntCircuitIngredient;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.CHEMICAL_RECIPES;
import static gregtech.api.recipes.RecipeMaps.LARGE_CHEMICAL_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;
import static gregtech.api.unification.ore.OrePrefix.dustTiny;

public class PolymerRecipes {

    public static void init() {
        polybenzimidazoleProcess();
    }

    private static void polybenzimidazoleProcess() {
        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[IV]).duration(100)
                .fluidInputs(Diaminobenzidine.getFluid(1000))
                .fluidInputs(DiphenylIsophtalate.getFluid(1000))
                .fluidOutputs(Phenol.getFluid(1000))
                .fluidOutputs(Polybenzimidazole.getFluid(1000))
                .buildAndRegister();

        // 3,3-Diaminobenzidine
        LARGE_CHEMICAL_RECIPES.recipeBuilder().EUt(VA[IV]).duration(100)
                .fluidInputs(Dichlorobenzidine.getFluid(1000))
                .fluidInputs(Ammonia.getFluid(2000))
                .notConsumable(dust, Zinc)
                .fluidOutputs(Diaminobenzidine.getFluid(1000))
                .fluidOutputs(HydrochloricAcid.getFluid(2000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[EV]).duration(200)
                .input(dustTiny, Copper)
                .fluidInputs(Nitrochlorobenzene.getFluid(1000))
                .fluidOutputs(Dichlorobenzidine.getFluid(1000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[EV]).duration(1800)
                .input(dust, Copper)
                .fluidInputs(Nitrochlorobenzene.getFluid(9000))
                .fluidOutputs(Dichlorobenzidine.getFluid(9000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[HV]).duration(100)
                .fluidInputs(NitrationMixture.getFluid(2000))
                .fluidInputs(Chlorobenzene.getFluid(1000))
                .fluidOutputs(Nitrochlorobenzene.getFluid(1000))
                .fluidOutputs(DilutedSulfuricAcid.getFluid(1000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[LV]).duration(240)
                .fluidInputs(Chlorine.getFluid(2000))
                .fluidInputs(Benzene.getFluid(1000))
                .notConsumable(new IntCircuitIngredient(1))
                .fluidOutputs(Chlorobenzene.getFluid(1000))
                .fluidOutputs(HydrochloricAcid.getFluid(1000))
                .buildAndRegister();

        // Diphenyl Isophthalate
        LARGE_CHEMICAL_RECIPES.recipeBuilder().EUt(VA[IV]).duration(100)
                .fluidInputs(Phenol.getFluid(2000))
                .fluidInputs(SulfuricAcid.getFluid(1000))
                .fluidInputs(PhthalicAcid.getFluid(1000))
                .fluidOutputs(DiphenylIsophtalate.getFluid(1000))
                .fluidOutputs(DilutedSulfuricAcid.getFluid(1000))
                .buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder().EUt(VA[EV]).duration(100)
                .input(dustTiny, PotassiumDichromate)
                .fluidInputs(Dimethylbenzene.getFluid(1000))
                .fluidInputs(Oxygen.getFluid(2000))
                .fluidOutputs(PhthalicAcid.getFluid(1000))
                .fluidOutputs(Water.getFluid(2000))
                .buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder().EUt(VA[EV]).duration(900)
                .input(dust, PotassiumDichromate)
                .fluidInputs(Dimethylbenzene.getFluid(9000))
                .fluidInputs(Oxygen.getFluid(18000))
                .fluidOutputs(PhthalicAcid.getFluid(9000))
                .fluidOutputs(Water.getFluid(18000))
                .buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder().EUt(VA[LV]).duration(125)
                .fluidInputs(Naphthalene.getFluid(2000))
                .fluidInputs(SulfuricAcid.getFluid(1000))
                .input(dustTiny, Potassium)
                .fluidOutputs(PhthalicAcid.getFluid(2500))
                .fluidOutputs(HydrogenSulfide.getFluid(1000))
                .buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder().EUt(VA[LV]).duration(1125)
                .fluidInputs(Naphthalene.getFluid(18000))
                .fluidInputs(SulfuricAcid.getFluid(9000))
                .input(dust, Potassium)
                .fluidOutputs(PhthalicAcid.getFluid(22500))
                .fluidOutputs(HydrogenSulfide.getFluid(9000))
                .buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder().EUt(VA[MV]).duration(4000)
                .fluidInputs(Methane.getFluid(1000))
                .fluidInputs(Benzene.getFluid(1000))
                .notConsumable(new IntCircuitIngredient(1))
                .fluidOutputs(Dimethylbenzene.getFluid(1000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[HV]).duration(100)
                .input(dust, Saltpeter, 10)
                .input(dust, ChromiumTrioxide, 8)
                .output(dust, PotassiumDichromate, 11)
                .fluidOutputs(NitrogenDioxide.getFluid(2000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().EUt(60).duration(100)
                .input(dust, Chrome)
                .fluidInputs(Oxygen.getFluid(3000))
                .output(dust, ChromiumTrioxide, 4)
                .buildAndRegister();
    }
}
