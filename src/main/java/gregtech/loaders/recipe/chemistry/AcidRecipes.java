package gregtech.loaders.recipe.chemistry;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.CHEMICAL_RECIPES;
import static gregtech.api.recipes.RecipeMaps.LARGE_CHEMICAL_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;

public class AcidRecipes {

    public static void init() {
        sulfuricAcidRecipes();
        nitricAcidRecipes();
        phosphoricAcidRecipes();
        aceticAcidRecipes();
    }

    private static void sulfuricAcidRecipes() {
        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .input(dust, Sulfur)
                .fluidInputs(Oxygen.getFluid(2000))
                .fluidOutputs(SulfurDioxide.getFluid(1000))
                .duration(60).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(Oxygen.getFluid(3000))
                .fluidInputs(HydrogenSulfide.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000))
                .fluidOutputs(SulfurDioxide.getFluid(1000))
                .duration(120).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(SulfurDioxide.getFluid(1000))
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidOutputs(SulfurTrioxide.getFluid(1000))
                .duration(200).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(SulfurTrioxide.getFluid(1000))
                .fluidInputs(Water.getFluid(1000))
                .fluidOutputs(SulfuricAcid.getFluid(1000))
                .duration(160).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .fluidInputs(HydrogenSulfide.getFluid(1000))
                .fluidInputs(Oxygen.getFluid(4000))
                .fluidOutputs(SulfuricAcid.getFluid(1000))
                .duration(320).EUt(VA[LV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(24)
                .input(dust, Sulfur)
                .fluidInputs(Water.getFluid(4000))
                .fluidOutputs(SulfuricAcid.getFluid(1000))
                .EUt(VA[HV])
                .duration(320)
                .buildAndRegister();
    }

    private static void nitricAcidRecipes() {
        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(Hydrogen.getFluid(3000))
                .fluidInputs(Nitrogen.getFluid(1000))
                .fluidOutputs(Ammonia.getFluid(1000))
                .duration(320).EUt(384).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(Oxygen.getFluid(5000))
                .fluidInputs(Ammonia.getFluid(2000))
                .fluidOutputs(NitricOxide.getFluid(2000))
                .fluidOutputs(Water.getFluid(3000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidInputs(NitricOxide.getFluid(1000))
                .fluidOutputs(NitrogenDioxide.getFluid(1000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(3)
                .fluidInputs(Nitrogen.getFluid(1000))
                .fluidInputs(Oxygen.getFluid(2000))
                .fluidOutputs(NitrogenDioxide.getFluid(1000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(NitrogenDioxide.getFluid(3000))
                .fluidInputs(Water.getFluid(1000))
                .fluidOutputs(NitricAcid.getFluid(2000))
                .fluidOutputs(NitricOxide.getFluid(1000))
                .duration(240).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(3)
                .fluidInputs(Water.getFluid(1000))
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidInputs(NitrogenDioxide.getFluid(2000))
                .fluidOutputs(NitricAcid.getFluid(2000))
                .duration(240).EUt(VA[LV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(24)
                .fluidInputs(Oxygen.getFluid(4000))
                .fluidInputs(Ammonia.getFluid(1000))
                .fluidOutputs(NitricAcid.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000))
                .duration(320).EUt(VA[LV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(24)
                .fluidInputs(Nitrogen.getFluid(1000))
                .fluidInputs(Hydrogen.getFluid(3000))
                .fluidInputs(Oxygen.getFluid(4000))
                .fluidOutputs(NitricAcid.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000))
                .duration(320).EUt(VA[HV]).buildAndRegister();
    }

    private static void phosphoricAcidRecipes() {
        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .input(dust, Phosphorus, 4)
                .fluidInputs(Oxygen.getFluid(10000))
                .output(dust, PhosphorusPentoxide, 14)
                .duration(40).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, PhosphorusPentoxide, 14)
                .fluidInputs(Water.getFluid(6000))
                .fluidOutputs(PhosphoricAcid.getFluid(4000))
                .duration(40).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Apatite, 9)
                .fluidInputs(SulfuricAcid.getFluid(5000))
                .fluidInputs(Water.getFluid(10000))
                .output(dust, Gypsum, 40)
                .fluidOutputs(HydrochloricAcid.getFluid(1000))
                .fluidOutputs(PhosphoricAcid.getFluid(3000))
                .duration(320).EUt(VA[LV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(24)
                .input(dust, Phosphorus, 2)
                .fluidInputs(Water.getFluid(3000))
                .fluidInputs(Oxygen.getFluid(5000))
                .fluidOutputs(PhosphoricAcid.getFluid(2000))
                .duration(320).EUt(VA[LV]).buildAndRegister();
    }

    private static void aceticAcidRecipes() {}
}
