package gregtech.integration.forestry.recipes;

import forestry.api.recipes.RecipeManagers;
import forestry.apiculture.ModuleApiculture;
import forestry.apiculture.items.EnumPropolis;
import forestry.core.config.Config;
import forestry.core.fluids.Fluids;
import forestry.factory.MachineUIDs;
import forestry.factory.ModuleFactory;
import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.common.items.MetaItems;
import gregtech.integration.IntegrationUtil;
import gregtech.integration.forestry.ForestryConfig;
import gregtech.integration.forestry.ForestryUtil;
import gregtech.integration.forestry.bees.GTDropType;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

public class MiscRecipes {

    public static void init() {

        // Extra Bees Propolis
        if (Loader.isModLoaded(GTValues.MODID_EB)) {
            RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                    .inputs(IntegrationUtil.getModItem(GTValues.MODID_EB, "propolis", 0))
                    .fluidOutputs(Materials.Water.getFluid(500))
                    .duration(32).EUt(7).buildAndRegister();

            RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                    .inputs(IntegrationUtil.getModItem(GTValues.MODID_EB, "propolis", 1))
                    .fluidOutputs(Materials.Oil.getFluid(500))
                    .duration(32).EUt(7).buildAndRegister();

            RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                    .inputs(IntegrationUtil.getModItem(GTValues.MODID_EB, "propolis", 7))
                    .fluidOutputs(Materials.Creosote.getFluid(500))
                    .duration(32).EUt(7).buildAndRegister();
        }

        if (ForestryConfig.enableGTBees) {
            // Oil Drop
            ItemStack dropStack = ForestryUtil.getDropStack(GTDropType.OIL);
            RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                    .inputs(dropStack)
                    .chancedOutput(ModuleApiculture.getItems().propolis.get(EnumPropolis.NORMAL, 1), 3000, 0)
                    .fluidOutputs(Materials.OilHeavy.getFluid(100))
                    .duration(32).EUt(8).buildAndRegister();

            if (ModuleFactory.machineEnabled(MachineUIDs.SQUEEZER)) {
                RecipeManagers.squeezerManager.addRecipe(40, dropStack, Materials.OilHeavy.getFluid(100), ModuleApiculture.getItems().propolis.get(EnumPropolis.NORMAL, 1), 30);
            }

            // Biomass Drop
            dropStack = ForestryUtil.getDropStack(GTDropType.BIOMASS);
            ItemStack propolisStack = ModuleApiculture.getItems().propolis.get(EnumPropolis.NORMAL, 1);
            if (Loader.isModLoaded(GTValues.MODID_EB)) {
                propolisStack = IntegrationUtil.getModItem(GTValues.MODID_EB, "propolis", 7);
            }
            RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                    .inputs(dropStack)
                    .chancedOutput(propolisStack, 3000, 0)
                    .fluidOutputs(Materials.Biomass.getFluid(100))
                    .duration(32).EUt(8).buildAndRegister();

            if (ModuleFactory.machineEnabled(MachineUIDs.SQUEEZER)) {
                RecipeManagers.squeezerManager.addRecipe(40, dropStack, Materials.Biomass.getFluid(100), propolisStack, 30);
            }

            // Ethanol Drop
            dropStack = ForestryUtil.getDropStack(GTDropType.ETHANOL);
            RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                    .inputs(dropStack)
                    .chancedOutput(propolisStack, 3000, 0)
                    .fluidOutputs(Materials.Ethanol.getFluid(100))
                    .duration(32).EUt(8).buildAndRegister();

            if (ModuleFactory.machineEnabled(MachineUIDs.SQUEEZER)) {
                RecipeManagers.squeezerManager.addRecipe(40, dropStack, Materials.Ethanol.getFluid(100), propolisStack, 30);
            }

            // Mutagen Drop
            dropStack = ForestryUtil.getDropStack(GTDropType.MUTAGEN);
            RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                    .inputs(dropStack)
                    .chancedOutput(propolisStack, 3000, 0)
                    .fluidOutputs(Materials.Mutagen.getFluid(100))
                    .duration(32).EUt(8).buildAndRegister();

            if (ModuleFactory.machineEnabled(MachineUIDs.SQUEEZER)) {
                RecipeManagers.squeezerManager.addRecipe(40, dropStack, Materials.Mutagen.getFluid(100), propolisStack, 30);
            }
        }

        // Honey and Juice recipes
        if (Config.isFluidEnabled(Fluids.FOR_HONEY)) {

            RecipeMaps.BREWING_RECIPES.recipeBuilder()
                    .input(MetaItems.PLANT_BALL)
                    .fluidInputs(Fluids.FOR_HONEY.getFluid(180))
                    .fluidOutputs(Materials.Biomass.getFluid(270))
                    .duration(1440).EUt(3).buildAndRegister();

            RecipeMaps.BREWING_RECIPES.recipeBuilder()
                    .input("treeSapling", 1)
                    .fluidInputs(Fluids.FOR_HONEY.getFluid(100))
                    .fluidOutputs(Materials.Biomass.getFluid(150))
                    .duration(600).EUt(3).buildAndRegister();

            RecipeMaps.BREWING_RECIPES.recipeBuilder()
                    .input(Items.POTATO)
                    .fluidInputs(Fluids.FOR_HONEY.getFluid(20))
                    .fluidOutputs(Materials.Biomass.getFluid(30))
                    .duration(160).EUt(3).buildAndRegister();

            RecipeMaps.BREWING_RECIPES.recipeBuilder()
                    .input(Items.CARROT)
                    .fluidInputs(Fluids.FOR_HONEY.getFluid(20))
                    .fluidOutputs(Materials.Biomass.getFluid(30))
                    .duration(160).EUt(3).buildAndRegister();

            RecipeMaps.BREWING_RECIPES.recipeBuilder()
                    .input(Blocks.CACTUS)
                    .fluidInputs(Fluids.FOR_HONEY.getFluid(20))
                    .fluidOutputs(Materials.Biomass.getFluid(30))
                    .duration(160).EUt(3).buildAndRegister();

            RecipeMaps.BREWING_RECIPES.recipeBuilder()
                    .input(Items.REEDS)
                    .fluidInputs(Fluids.FOR_HONEY.getFluid(20))
                    .fluidOutputs(Materials.Biomass.getFluid(30))
                    .duration(160).EUt(3).buildAndRegister();

            RecipeMaps.BREWING_RECIPES.recipeBuilder()
                    .input(Blocks.BROWN_MUSHROOM)
                    .fluidInputs(Fluids.FOR_HONEY.getFluid(20))
                    .fluidOutputs(Materials.Biomass.getFluid(30))
                    .duration(160).EUt(3).buildAndRegister();

            RecipeMaps.BREWING_RECIPES.recipeBuilder()
                    .input(Blocks.RED_MUSHROOM)
                    .fluidInputs(Fluids.FOR_HONEY.getFluid(20))
                    .fluidOutputs(Materials.Biomass.getFluid(30))
                    .duration(160).EUt(3).buildAndRegister();

            RecipeMaps.BREWING_RECIPES.recipeBuilder()
                    .input(Items.BEETROOT)
                    .fluidInputs(Fluids.FOR_HONEY.getFluid(20))
                    .fluidOutputs(Materials.Biomass.getFluid(30))
                    .duration(160).EUt(3).buildAndRegister();
        }

        if (Config.isFluidEnabled(Fluids.JUICE)) {

            RecipeMaps.BREWING_RECIPES.recipeBuilder()
                    .input(MetaItems.PLANT_BALL)
                    .fluidInputs(Fluids.JUICE.getFluid(180))
                    .fluidOutputs(Materials.Biomass.getFluid(270))
                    .duration(1440).EUt(3).buildAndRegister();

            RecipeMaps.BREWING_RECIPES.recipeBuilder()
                    .input("treeSapling", 1)
                    .fluidInputs(Fluids.JUICE.getFluid(100))
                    .fluidOutputs(Materials.Biomass.getFluid(150))
                    .duration(600).EUt(3).buildAndRegister();

            RecipeMaps.BREWING_RECIPES.recipeBuilder()
                    .input(Items.POTATO)
                    .fluidInputs(Fluids.JUICE.getFluid(20))
                    .fluidOutputs(Materials.Biomass.getFluid(30))
                    .duration(160).EUt(3).buildAndRegister();

            RecipeMaps.BREWING_RECIPES.recipeBuilder()
                    .input(Items.CARROT)
                    .fluidInputs(Fluids.JUICE.getFluid(20))
                    .fluidOutputs(Materials.Biomass.getFluid(30))
                    .duration(160).EUt(3).buildAndRegister();

            RecipeMaps.BREWING_RECIPES.recipeBuilder()
                    .input(Blocks.CACTUS)
                    .fluidInputs(Fluids.JUICE.getFluid(20))
                    .fluidOutputs(Materials.Biomass.getFluid(30))
                    .duration(160).EUt(3).buildAndRegister();

            RecipeMaps.BREWING_RECIPES.recipeBuilder()
                    .input(Items.REEDS)
                    .fluidInputs(Fluids.JUICE.getFluid(20))
                    .fluidOutputs(Materials.Biomass.getFluid(30))
                    .duration(160).EUt(3).buildAndRegister();

            RecipeMaps.BREWING_RECIPES.recipeBuilder()
                    .input(Blocks.BROWN_MUSHROOM)
                    .fluidInputs(Fluids.JUICE.getFluid(20))
                    .fluidOutputs(Materials.Biomass.getFluid(30))
                    .duration(160).EUt(3).buildAndRegister();

            RecipeMaps.BREWING_RECIPES.recipeBuilder()
                    .input(Blocks.RED_MUSHROOM)
                    .fluidInputs(Fluids.JUICE.getFluid(20))
                    .fluidOutputs(Materials.Biomass.getFluid(30))
                    .duration(160).EUt(3).buildAndRegister();

            RecipeMaps.BREWING_RECIPES.recipeBuilder()
                    .input(Items.BEETROOT)
                    .fluidInputs(Fluids.JUICE.getFluid(20))
                    .fluidOutputs(Materials.Biomass.getFluid(30))
                    .duration(160).EUt(3).buildAndRegister();
        }
    }
}
