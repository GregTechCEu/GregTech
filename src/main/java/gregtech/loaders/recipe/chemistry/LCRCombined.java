package gregtech.loaders.recipe.chemistry;

import gregtech.api.recipes.ingredients.IntCircuitIngredient;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.LARGE_CHEMICAL_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;

public class LCRCombined {

    static void init() {

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(24))
                .fluidInputs(Methane.getFluid(3000))
                .fluidInputs(Nitrogen.getFluid(4000))
                .fluidInputs(Oxygen.getFluid(3000))
                .fluidOutputs(Ammonia.getFluid(4000))
                .fluidOutputs(CarbonMonoxide.getFluid(3000))
                .EUt(VA[HV])
                .duration(320)
                .buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(24))
                .fluidInputs(Hydrogen.getFluid(6000))
                .fluidInputs(CarbonMonoxide.getFluid(1000))
                .fluidOutputs(Methane.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000))
                .EUt(VA[LV])
                .duration(160)
                .buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(24))
                .fluidInputs(Propene.getFluid(1000))
                .fluidInputs(Benzene.getFluid(1000))
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidInputs(PhosphoricAcid.getFluid(100))
                .fluidOutputs(Phenol.getFluid(1000))
                .fluidOutputs(Acetone.getFluid(1000))
                .duration(480).EUt(VA[LV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(24))
                .fluidInputs(Benzene.getFluid(1000))
                .fluidInputs(Chlorine.getFluid(2000))
                .fluidInputs(Water.getFluid(1000))
                .fluidOutputs(Phenol.getFluid(1000))
                .fluidOutputs(HydrochloricAcid.getFluid(1000))
                .fluidOutputs(DilutedHydrochloricAcid.getFluid(1000))
                .duration(560).EUt(VA[LV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(24))
                .fluidInputs(Benzene.getFluid(2000))
                .fluidInputs(Chlorine.getFluid(4000))
                .input(dust, SodiumHydroxide, 6)
                .output(dust, Salt, 4)
                .fluidOutputs(Phenol.getFluid(2000))
                .fluidOutputs(HydrochloricAcid.getFluid(2000))
                .duration(1120).EUt(VA[LV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(24))
                .fluidInputs(LightFuel.getFluid(20000))
                .fluidInputs(HeavyFuel.getFluid(4000))
                .fluidOutputs(Diesel.getFluid(24000))
                .duration(100).EUt(VA[HV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(24))
                .fluidInputs(Diesel.getFluid(10000))
                .fluidInputs(Tetranitromethane.getFluid(200))
                .fluidOutputs(CetaneBoostedDiesel.getFluid(10000))
                .duration(120).EUt(VA[HV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(24))
                .fluidInputs(BioDiesel.getFluid(10000))
                .fluidInputs(Tetranitromethane.getFluid(400))
                .fluidOutputs(CetaneBoostedDiesel.getFluid(7500))
                .duration(120).EUt(VA[HV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(AceticAcid.getFluid(3000))
                .notConsumable(dust, Quicklime)
                .notConsumable(new IntCircuitIngredient(24))
                .fluidOutputs(Acetone.getFluid(2000))
                .fluidOutputs(Oxygen.getFluid(1000))
                .duration(400).EUt(VA[HV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(5))
                .fluidInputs(Oxygen.getFluid(7000))
                .fluidInputs(Nitrogen.getFluid(2000))
                .fluidInputs(Hydrogen.getFluid(6000))
                .fluidOutputs(DinitrogenTetroxide.getFluid(1000))
                .fluidOutputs(Water.getFluid(3000))
                .duration(1100).EUt(VA[HV]).buildAndRegister();
    }
}
