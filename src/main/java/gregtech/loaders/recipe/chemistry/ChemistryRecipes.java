package gregtech.loaders.recipe.chemistry;

import gregtech.api.fluids.store.FluidStorageKeys;

import gregtech.api.unification.material.Material;

import net.minecraft.init.Items;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;
import static gregtech.api.unification.ore.OrePrefix.ingot;

public class ChemistryRecipes {

    public static void init() {
        PetrochemRecipes.init();
        DistillationRecipes.init();
        SeparationRecipes.init();
        MixerRecipes.init();
        BrewingRecipes.init();
        ChemicalBathRecipes.init();
        ReactorRecipes.init();
        PolymerRecipes.init();
        LCRCombined.init();
        GrowthMediumRecipes.init();
        NuclearRecipes.init();
        FuelRecipeChains.init();
        GemSlurryRecipes.init();
        PlatGroupMetalsRecipes.init();
        NaquadahRecipes.init();
        AcidRecipes.init();
        TitaniumRecipes.init();

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

        vacuumAir(CarbonDioxide, 1);
        vacuumAir(Ammonia, 1);
        vacuumAir(Radon, 2);
        vacuumAir(Krypton, 2);
        vacuumAir(Nitrogen, 3);
        vacuumAir(Argon, 3);
        vacuumAir(Xenon, 3);
        vacuumAir(Methane, 3);
        vacuumAir(Oxygen, 4);
        vacuumAir(Helium, 4);
        vacuumAir(Hydrogen, 4);
        vacuumAir(Neon, 4);
        vacuumAir(Helium, 4);

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
                .circuitMeta(1)
                .fluidOutputs(Air.getFluid(10000))
                .dimension(0)
                .duration(200).EUt(16).buildAndRegister();

        GAS_COLLECTOR_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .fluidOutputs(NetherAir.getFluid(10000))
                .dimension(-1)
                .duration(200).EUt(64).buildAndRegister();

        GAS_COLLECTOR_RECIPES.recipeBuilder()
                .circuitMeta(3)
                .fluidOutputs(EnderAir.getFluid(10000))
                .dimension(1)
                .duration(200).EUt(256).buildAndRegister();
    }

    private static void vacuumAir(Material material, int i) {
        VACUUM_RECIPES.recipeBuilder()
                .fluidInputs(material.getFluid(FluidStorageKeys.GAS, 1000))
                .fluidOutputs(material.getFluid(FluidStorageKeys.LIQUID, 1000))
                .duration((int) (60*Math.pow(2,i+1))).EUt(VA[i]).buildAndRegister();

        FLUID_HEATER_RECIPES.recipeBuilder()
                .fluidInputs(material.getFluid(FluidStorageKeys.LIQUID, 4000))
                .fluidOutputs(material.getFluid(FluidStorageKeys.GAS, 4000))
                .duration((int) (20*Math.pow(2,i))).EUt(VA[i]).buildAndRegister();
    }
}
