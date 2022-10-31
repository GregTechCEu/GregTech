package gregtech.loaders.recipe.chemistry;

import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.loaders.recipe.GTRecipeLoaders;
import net.minecraft.init.Items;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;
import static gregtech.api.unification.ore.OrePrefix.ingot;

public class ChemistryRecipes {

    public static void init() {

        GTRecipeLoaders.PETROCHEM.register(PetrochemRecipes::init);
        GTRecipeLoaders.DISTILLATION.register(DistillationRecipes::init);
        SeparationRecipes.init();
        MixerRecipes.init();
        BrewingRecipes.init();
        ChemicalBathRecipes.init();
        GTRecipeLoaders.REACTORS.register(ReactorRecipes::init);
        PolymerRecipes.init();
        GTRecipeLoaders.LCR.register(LCRCombined::init);
        GTRecipeLoaders.GROWTH_MEDIUM.register(GrowthMediumRecipes::init);
        GTRecipeLoaders.URANIUM_REFINING.register(NuclearRecipes::init);
        GTRecipeLoaders.FUELS.register(FuelRecipeChains::init);
        GemSlurryRecipes.init();
        GTRecipeLoaders.PLATINUM_REFINING.register(PlatGroupMetalsRecipes::init);
        GTRecipeLoaders.NAQUADAH_REFINING.register(NaquadahRecipes::init);
        AcidRecipes.init();


        // A Few Random Recipes
        FLUID_HEATER_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(Acetone.getFluid(100))
                .fluidOutputs(Ethenone.getFluid(100))
                .duration(16).EUt(VA[LV]).buildAndRegister();

        FLUID_HEATER_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(DissolvedCalciumAcetate.getFluid(200))
                .fluidOutputs(Acetone.getFluid(200))
                .duration(16).EUt(VA[LV]).buildAndRegister();

        VACUUM_RECIPES.recipeBuilder()
                .fluidInputs(Water.getFluid(1000))
                .fluidOutputs(Ice.getFluid(1000))
                .duration(50).EUt(VA[LV]).buildAndRegister();

        if (GTRecipeLoaders.GAS_LIQUEFACTION.shouldRegister()) {
            VACUUM_RECIPES.recipeBuilder()
                    .fluidInputs(Air.getFluid(4000))
                    .fluidOutputs(LiquidAir.getFluid(4000))
                    .duration(80).EUt(VA[HV]).buildAndRegister();

            VACUUM_RECIPES.recipeBuilder()
                    .fluidInputs(NetherAir.getFluid(4000))
                    .fluidOutputs(LiquidNetherAir.getFluid(4000))
                    .duration(80).EUt(VA[EV]).buildAndRegister();

            VACUUM_RECIPES.recipeBuilder()
                    .fluidInputs(EnderAir.getFluid(4000))
                    .fluidOutputs(LiquidEnderAir.getFluid(4000))
                    .duration(80).EUt(VA[IV]).buildAndRegister();

            VACUUM_RECIPES.recipeBuilder()
                    .fluidInputs(Oxygen.getFluid(1000))
                    .fluidOutputs(LiquidOxygen.getFluid(1000))
                    .duration(240).EUt(VA[EV]).buildAndRegister();

            VACUUM_RECIPES.recipeBuilder()
                    .fluidInputs(Helium.getFluid(1000))
                    .fluidOutputs(LiquidHelium.getFluid(1000))
                    .duration(240).EUt(VA[EV]).buildAndRegister();
        }

        BLAST_RECIPES.recipeBuilder()
                .input(dust, FerriteMixture)
                .fluidInputs(Oxygen.getFluid(2000))
                .output(ingot, NickelZincFerrite)
                .blastFurnaceTemp(1500)
                .duration(400).EUt(VA[MV]).buildAndRegister();

        FERMENTING_RECIPES.recipeBuilder()
                .fluidInputs(Biomass.getFluid(100))
                .fluidOutputs(FermentedBiomass.getFluid(100))
                .duration(150).EUt(2).buildAndRegister();

        WIREMILL_RECIPES.recipeBuilder()
                .input(ingot, Polycaprolactam)
                .output(Items.STRING, 32)
                .duration(80).EUt(48).buildAndRegister();

        GAS_COLLECTOR_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .fluidOutputs(Air.getFluid(10000))
                .dimension(0)
                .duration(200).EUt(16).buildAndRegister();

        GAS_COLLECTOR_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .fluidOutputs(NetherAir.getFluid(10000))
                .dimension(-1)
                .duration(200).EUt(64).buildAndRegister();

        GAS_COLLECTOR_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(3))
                .fluidOutputs(EnderAir.getFluid(10000))
                .dimension(1)
                .duration(200).EUt(256).buildAndRegister();

        // CaCO3 + 2NaCl -> Na2CO3 + CaCl2
        BLAST_RECIPES.recipeBuilder()
                .input(dust, Calcite, 5)
                .input(dust, Salt, 4)
                .output(dust, SodaAsh, 6)
                .output(dust, CalciumChloride, 3)
                .duration(120).EUt(VA[MV]).blastFurnaceTemp(1500)
                .buildAndRegister();

        // 2NaOH + CO2 -> Na2CO3 + H20
        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, SodiumHydroxide, 6)
                .fluidInputs(CarbonDioxide.getFluid(1000))
                .output(dust, SodaAsh, 6)
                .fluidOutputs(Water.getFluid(1000))
                .duration(80).EUt(VA[HV])
                .buildAndRegister();
    }
}
