package gregtech.loaders.recipe.chemistry;

import gregtech.loaders.recipe.LargeReactorRecipes;
import net.minecraft.init.Items;

import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;
import static gregtech.api.unification.ore.OrePrefix.ingot;

public class ChemistryRecipes {

    public static void init() {

        CrackingRecipes.init();
        DistillationRecipes.init();
        SeparationRecipes.init();
        MixerRecipes.init();
        BrewingRecipes.init();
        ChemicalBathRecipes.init();
        ReactorRecipes.init();
        PolymerRecipes.init();
        LargeReactorRecipes.init();
        GrowthMediumRecipes.init();


        // A Few Random Recipes
        FLUID_HEATER_RECIPES.recipeBuilder()
            .circuitMeta(1)
            .fluidInputs(Acetone.getFluid(100))
            .fluidOutputs(Ethenone.getFluid(100))
            .duration(16).EUt(30).buildAndRegister();

        FLUID_HEATER_RECIPES.recipeBuilder()
            .circuitMeta(1)
            .fluidInputs(DissolvedCalciumAcetate.getFluid(200))
            .fluidOutputs(Acetone.getFluid(200))
            .duration(16).EUt(30).buildAndRegister();

        VACUUM_RECIPES.recipeBuilder()
            .fluidInputs(Water.getFluid(1000))
            .fluidOutputs(Ice.getFluid(1000))
            .duration(50).EUt(30).buildAndRegister();

        VACUUM_RECIPES.recipeBuilder()
            .fluidInputs(Air.getFluid(4000))
            .fluidOutputs(LiquidAir.getFluid(4000))
            .duration(400).EUt(30).buildAndRegister();

        BLAST_RECIPES.recipeBuilder()
            .input(dust, FerriteMixture, 6)
            .fluidInputs(Oxygen.getFluid(8000))
            .output(ingot, NickelZincFerrite, 14)
            .blastFurnaceTemp(1500)
            .duration(3200).EUt(120).buildAndRegister();

        FERMENTING_RECIPES.recipeBuilder()
            .fluidInputs(Biomass.getFluid(100))
            .fluidOutputs(FermentedBiomass.getFluid(100))
            .duration(150).EUt(2).buildAndRegister();

        WIREMILL_RECIPES.recipeBuilder()
            .input(ingot, Polycaprolactam)
            .output(Items.STRING, 32)
            .duration(80).EUt(48).buildAndRegister();

        BLAST_RECIPES.recipeBuilder()
            .input(Items.REDSTONE)
            .input(ingot, Copper)
            .output(ingot, RedAlloy, 2)
            .blastFurnaceTemp(1200)
            .duration(884).EUt(120).buildAndRegister();

        BLAST_RECIPES.recipeBuilder()
            .input(Items.REDSTONE)
            .input(dust, Copper)
            .output(ingot, RedAlloy, 2)
            .blastFurnaceTemp(1200)
            .duration(884).EUt(120).buildAndRegister();
    }
}
