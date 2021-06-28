package gregtech.loaders.recipe.chemistry;

import gregtech.api.GTValues;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class SeparationRecipes {

    public static void init() {

        // Centrifuge
        CENTRIFUGE_RECIPES.recipeBuilder()
            .fluidInputs(RefineryGas.getFluid(8000))
            .fluidOutputs(Methane.getFluid(4000))
            .fluidOutputs(LPG.getFluid(4000))
            .duration(200).EUt(5).buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder()
            .fluidInputs(LiquidAir.getFluid(53000))
            .fluidOutputs(Nitrogen.getFluid(32000))
            .fluidOutputs(Nitrogen.getFluid(8000))
            .fluidOutputs(Oxygen.getFluid(11000))
            .fluidOutputs(Argon.getFluid(1000))
            .fluidOutputs(NobleGases.getFluid(1000))
            .duration(1484).EUt(5).buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder()
            .fluidInputs(NobleGases.getFluid(34000))
            .fluidOutputs(CarbonDioxide.getFluid(21000))
            .fluidOutputs(Helium.getFluid(9000))
            .fluidOutputs(Methane.getFluid(3000))
            .fluidOutputs(Deuterium.getFluid(1000))
            .duration(680).EUt(5).buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder()
            .fluidInputs(Butane.getFluid(320))
            .fluidOutputs(LPG.getFluid(370))
            .duration(20).EUt(5).buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder()
            .fluidInputs(Propane.getFluid(320))
            .fluidOutputs(LPG.getFluid(290))
            .duration(20).EUt(5).buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder()
            .fluidInputs(NitrationMixture.getFluid(2000))
            .fluidOutputs(NitricAcid.getFluid(1000))
            .fluidOutputs(SulfuricAcid.getFluid(1000))
            .duration(192).EUt(30).buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder()
            .input(dust, ReinforcedEpoxyResin)
            .output(dust, Epoxy)
            .duration(24).EUt(5).buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder()
            .input(OrePrefix.ore, Oilsands)
            .chancedOutput(new ItemStack(Blocks.SAND), 5000, 5000)
            .fluidOutputs(Oil.getFluid(500))
            .duration(1000).EUt(5).buildAndRegister();


        // Electrolyzer
        ELECTROLYZER_RECIPES.recipeBuilder()
            .input(dust, SodiumBisulfate, 14)
            .fluidOutputs(SodiumPersulfate.getFluid(1000))
            .fluidOutputs(Hydrogen.getFluid(2000))
            .duration(448).EUt(60).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
            .fluidInputs(SaltWater.getFluid(1000))
            .output(dust, SodiumHydroxide, 3)
            .fluidOutputs(Chlorine.getFluid(1000))
            .fluidOutputs(Hydrogen.getFluid(1000))
            .duration(720).EUt(30).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
            .input(dust, Sphalerite, 2)
            .output(dust, Zinc)
            .output(dust, Sulfur)
            .chancedOutput(OreDictUnifier.get(dustTiny, Gallium), 2500, 1000)
            .duration(200).EUt(30).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
            .input(dust, Bauxite, 39)
            .output(dust, Rutile, 6)
            .output(dust, Aluminium, 16)
            .fluidOutputs(Hydrogen.getFluid(10000))
            .fluidOutputs(Oxygen.getFluid(11000))
            .duration(2496).EUt(60).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
            .fluidInputs(Water.getFluid(1000))
            .fluidOutputs(Hydrogen.getFluid(2000))
            .fluidOutputs(Oxygen.getFluid(1000))
            .duration(1500).EUt(30).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
            .fluidInputs(DistilledWater.getFluid(1000))
            .fluidOutputs(Hydrogen.getFluid(2000))
            .fluidOutputs(Oxygen.getFluid(1000))
            .duration(1500).EUt(30).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Items.DYE, 3))
            .output(dust, Calcium)
            .duration(96).EUt(26).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Blocks.SAND, 8))
            .output(dust, SiliconDioxide)
            .duration(500).EUt(25).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
            .input(dust, Graphite)
            .output(dust, Carbon, 4)
            .duration(100).EUt(26).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
            .fluidInputs(AceticAcid.getFluid(2000))
            .fluidOutputs(Ethane.getFluid(1000))
            .fluidOutputs(CarbonDioxide.getFluid(2000))
            .fluidOutputs(Hydrogen.getFluid(2000))
            .duration(512).EUt(60).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
            .fluidInputs(Chloromethane.getFluid(2000))
            .fluidOutputs(Ethane.getFluid(1000))
            .fluidOutputs(Chlorine.getFluid(2000))
            .duration(400).EUt(60).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
            .fluidInputs(Acetone.getFluid(2000))
            .output(dust, Carbon, 3)
            .fluidOutputs(Propane.getFluid(1000))
            .fluidOutputs(Water.getFluid(2000))
            .duration(480).EUt(60).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
            .fluidInputs(Butane.getFluid(1000))
            .fluidOutputs(Butene.getFluid(1000))
            .fluidOutputs(Hydrogen.getFluid(2000))
            .duration(240).EUt(120).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
            .fluidInputs(Butene.getFluid(1000))
            .fluidOutputs(Butadiene.getFluid(1000))
            .fluidOutputs(Hydrogen.getFluid(2000))
            .duration(240).EUt(120).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
            .fluidInputs(Propane.getFluid(1000))
            .fluidOutputs(Propene.getFluid(1000))
            .fluidOutputs(Hydrogen.getFluid(2000))
            .duration(640).EUt(120).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
            .input(dust, Diamond)
            .output(dust, Carbon, 64)
            .duration(768).EUt(30).buildAndRegister();

        // Thermal Centrifuge
        THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Blocks.COBBLESTONE, 1, GTValues.W))
            .output(dust, Stone)
            .duration(500).EUt(48).buildAndRegister();

        // Fluid Extractor
        FLUID_EXTRACTION_RECIPES.recipeBuilder()
            .input(dust, Monazite)
            .fluidOutputs(Helium.getFluid(200))
            .duration(64).EUt(64).buildAndRegister();
    }
}
