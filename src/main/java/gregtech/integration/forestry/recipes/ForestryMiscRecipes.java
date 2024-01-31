package gregtech.integration.forestry.recipes;

import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Mods;
import gregtech.common.items.MetaItems;
import gregtech.integration.forestry.ForestryConfig;
import gregtech.integration.forestry.ForestryUtil;
import gregtech.integration.forestry.bees.GTDropType;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import forestry.api.recipes.RecipeManagers;
import forestry.apiculture.ModuleApiculture;
import forestry.apiculture.items.EnumPropolis;
import forestry.core.config.Config;
import forestry.core.fluids.Fluids;
import forestry.factory.MachineUIDs;
import forestry.factory.ModuleFactory;

public class ForestryMiscRecipes {

    public static void init() {
        if (ForestryConfig.enableGTBees && Mods.ForestryApiculture.isModLoaded()) {
            // Oil Drop
            ItemStack dropStack = ForestryUtil.getDropStack(GTDropType.OIL);
            RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                    .inputs(dropStack)
                    .chancedOutput(ModuleApiculture.getItems().propolis.get(EnumPropolis.NORMAL, 1), 3000, 0)
                    .fluidOutputs(Materials.OilHeavy.getFluid(100))
                    .duration(32).EUt(8).buildAndRegister();

            if (ModuleFactory.machineEnabled(MachineUIDs.SQUEEZER)) {
                RecipeManagers.squeezerManager.addRecipe(40, dropStack, Materials.OilHeavy.getFluid(100),
                        ModuleApiculture.getItems().propolis.get(EnumPropolis.NORMAL, 1), 30);
            }

            // Biomass Drop
            dropStack = ForestryUtil.getDropStack(GTDropType.BIOMASS);
            ItemStack propolisStack = ModuleApiculture.getItems().propolis.get(EnumPropolis.NORMAL, 1);
            if (Mods.ExtraBees.isModLoaded()) {
                propolisStack = Mods.ExtraBees.getItem("propolis", 7);
            }
            RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                    .inputs(dropStack)
                    .chancedOutput(propolisStack, 3000, 0)
                    .fluidOutputs(Materials.Biomass.getFluid(100))
                    .duration(32).EUt(8).buildAndRegister();

            if (ModuleFactory.machineEnabled(MachineUIDs.SQUEEZER)) {
                RecipeManagers.squeezerManager.addRecipe(40, dropStack, Materials.Biomass.getFluid(100), propolisStack,
                        30);
            }

            // Ethanol Drop
            dropStack = ForestryUtil.getDropStack(GTDropType.ETHANOL);
            RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                    .inputs(dropStack)
                    .chancedOutput(propolisStack, 3000, 0)
                    .fluidOutputs(Materials.Ethanol.getFluid(100))
                    .duration(32).EUt(8).buildAndRegister();

            if (ModuleFactory.machineEnabled(MachineUIDs.SQUEEZER)) {
                RecipeManagers.squeezerManager.addRecipe(40, dropStack, Materials.Ethanol.getFluid(100), propolisStack,
                        30);
            }

            // Mutagen Drop
            dropStack = ForestryUtil.getDropStack(GTDropType.MUTAGEN);
            RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                    .inputs(dropStack)
                    .chancedOutput(propolisStack, 3000, 0)
                    .fluidOutputs(Materials.Mutagen.getFluid(100))
                    .duration(32).EUt(8).buildAndRegister();

            if (ModuleFactory.machineEnabled(MachineUIDs.SQUEEZER)) {
                RecipeManagers.squeezerManager.addRecipe(40, dropStack, Materials.Mutagen.getFluid(100), propolisStack,
                        30);
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

        // Fertilizer
        ItemStack fertilizer = Mods.Forestry.getItem("fertilizer_compound");
        RecipeMaps.MIXER_RECIPES.recipeBuilder()
                .input("sand", 2)
                .input(OrePrefix.dust, Materials.Apatite)
                .fluidInputs(Materials.Water.getFluid(100))
                .outputs(GTUtility.copy(5, fertilizer))
                .duration(100).EUt(16).buildAndRegister();

        RecipeMaps.MIXER_RECIPES.recipeBuilder()
                .input(OrePrefix.dust, Materials.Ash, 8)
                .input(OrePrefix.dust, Materials.Apatite)
                .fluidInputs(Materials.Water.getFluid(100))
                .outputs(GTUtility.copy(10, fertilizer))
                .duration(100).EUt(16).buildAndRegister();

        RecipeMaps.MIXER_RECIPES.recipeBuilder()
                .input(MetaItems.FERTILIZER, 8)
                .input(OrePrefix.dust, Materials.Apatite)
                .fluidInputs(Materials.Water.getFluid(1000))
                .outputs(GTUtility.copy(30, fertilizer))
                .duration(100).EUt(16).buildAndRegister();

        if (Mods.MagicBees.isModLoaded()) {
            ItemStack concentratedCompound = Mods.MagicBees.getItem("resource", 2);
            RecipeMaps.MIXER_RECIPES.recipeBuilder()
                    .input("sand", 2)
                    .inputs(GTUtility.copy(concentratedCompound))
                    .fluidInputs(Materials.Water.getFluid(100))
                    .outputs(GTUtility.copy(6, fertilizer))
                    .duration(100).EUt(16).buildAndRegister();

            RecipeMaps.MIXER_RECIPES.recipeBuilder()
                    .input(OrePrefix.dust, Materials.Ash, 8)
                    .inputs(GTUtility.copy(concentratedCompound))
                    .fluidInputs(Materials.Water.getFluid(100))
                    .outputs(GTUtility.copy(12, fertilizer))
                    .duration(100).EUt(16).buildAndRegister();

            RecipeMaps.MIXER_RECIPES.recipeBuilder()
                    .input(MetaItems.FERTILIZER, 8)
                    .inputs(GTUtility.copy(concentratedCompound))
                    .fluidInputs(Materials.Water.getFluid(1000))
                    .outputs(GTUtility.copy(32, fertilizer))
                    .duration(100).EUt(16).buildAndRegister();
        }

        // Compost
        ItemStack compost = Mods.Forestry.getItem("fertilizer_bio");
        RecipeMaps.MIXER_RECIPES.recipeBuilder()
                .input(Blocks.DIRT, 1, true)
                .input(Items.WHEAT, 4)
                .fluidInputs(Materials.Water.getFluid(100))
                .outputs(GTUtility.copy(4, compost))
                .duration(200).EUt(16).buildAndRegister();

        RecipeMaps.MIXER_RECIPES.recipeBuilder()
                .input(Blocks.DIRT, 1, true)
                .input(OrePrefix.dust, Materials.Ash, 4)
                .fluidInputs(Materials.Water.getFluid(100))
                .outputs(GTUtility.copy(4, compost))
                .duration(200).EUt(16).buildAndRegister();

        RecipeMaps.BREWING_RECIPES.recipeBuilder()
                .inputs(GTUtility.copy(2, compost))
                .fluidInputs(Materials.Water.getFluid(375))
                .fluidOutputs(Materials.Biomass.getFluid(375))
                .duration(64).EUt(4).buildAndRegister();

        RecipeMaps.PYROLYSE_RECIPES.recipeBuilder()
                .inputs(GTUtility.copy(4, compost))
                .circuitMeta(1)
                .fluidInputs(Materials.Water.getFluid(4000))
                .fluidOutputs(Materials.Biomass.getFluid(5000))
                .duration(900).EUt(10).buildAndRegister();

        // Phosphor
        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(Mods.Forestry.getItem("phosphor"))
                .chancedOutput(OrePrefix.dust, Materials.Phosphorus, 1000, 0)
                .fluidOutputs(Materials.Lava.getFluid(800))
                .duration(256).EUt(GTValues.VA[GTValues.MV]).buildAndRegister();

        // Ice Shard
        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(Mods.Forestry.getItem("crafting_material", 5))
                .output(OrePrefix.dust, Materials.Ice)
                .duration(16).EUt(4).buildAndRegister();

        // Mulch
        ItemStack mulch = Mods.Forestry.getItem("mulch");
        RecipeMaps.CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(MetaItems.BIO_CHAFF)
                .fluidInputs(Materials.Water.getFluid(750))
                .outputs(GTUtility.copy(8, mulch))
                .chancedOutput(GTUtility.copy(4, mulch), 3300, 0)
                .chancedOutput(GTUtility.copy(4, mulch), 2000, 0)
                .duration(500).EUt(GTValues.VA[GTValues.LV]).buildAndRegister();

        if (Mods.ExtraTrees.isModLoaded()) {
            RecipeMaps.MIXER_RECIPES.recipeBuilder()
                    .inputs(Mods.ExtraTrees.getItem("misc", 1))
                    .fluidInputs(Materials.Water.getFluid(500))
                    .outputs(GTUtility.copy(mulch))
                    .duration(600).EUt(2).buildAndRegister();
        }

        RecipeMaps.BREWING_RECIPES.recipeBuilder()
                .inputs(GTUtility.copy(8, mulch))
                .fluidInputs(Materials.DistilledWater.getFluid(375))
                .fluidOutputs(Materials.Biomass.getFluid(375))
                .duration(64).EUt(4).buildAndRegister();

        RecipeMaps.BREWING_RECIPES.recipeBuilder()
                .inputs(GTUtility.copy(4, mulch))
                .fluidInputs(Fluids.JUICE.getFluid(250))
                .fluidOutputs(Materials.Biomass.getFluid(375))
                .duration(64).EUt(4).buildAndRegister();

        RecipeMaps.PYROLYSE_RECIPES.recipeBuilder()
                .inputs(GTUtility.copy(32, mulch))
                .fluidInputs(Materials.Water.getFluid(4000))
                .circuitMeta(1)
                .fluidOutputs(Materials.Biomass.getFluid(5000))
                .duration(900).EUt(10).buildAndRegister();

        // Humus
        ItemStack humus = Mods.Forestry.getItem("humus");
        RecipeMaps.MIXER_RECIPES.recipeBuilder()
                .inputs(GTUtility.copy(2, mulch))
                .input(Blocks.DIRT, 2, true)
                .fluidInputs(Materials.Water.getFluid(250))
                .outputs(GTUtility.copy(2, humus))
                .duration(16).EUt(16).buildAndRegister();

        RecipeMaps.MIXER_RECIPES.recipeBuilder()
                .inputs(GTUtility.copy(compost))
                .input(Blocks.DIRT, 8, true)
                .fluidInputs(Materials.Water.getFluid(1000))
                .outputs(GTUtility.copy(8, humus))
                .duration(64).EUt(16).buildAndRegister();

        RecipeMaps.MIXER_RECIPES.recipeBuilder()
                .inputs(GTUtility.copy(fertilizer))
                .input(Blocks.DIRT, 8, true)
                .fluidInputs(Materials.Water.getFluid(1000))
                .outputs(GTUtility.copy(8, humus))
                .duration(64).EUt(16).buildAndRegister();

        RecipeMaps.MIXER_RECIPES.recipeBuilder()
                .input(MetaItems.FERTILIZER)
                .input(Blocks.DIRT, 8, true)
                .fluidInputs(Materials.Water.getFluid(1000))
                .outputs(GTUtility.copy(8, humus))
                .duration(64).EUt(16).buildAndRegister();

        // Cans, Capsules, etc
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.Tin, 2)
                .input(Blocks.GLASS_PANE)
                .circuitMeta(1)
                .outputs(Mods.Forestry.getItem("can"))
                .duration(120).EUt(7).buildAndRegister();

        RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                .inputs(Mods.Forestry.getItem("beeswax"))
                .notConsumable(MetaItems.SHAPE_EXTRUDER_CELL)
                .outputs(Mods.Forestry.getItem("capsule"))
                .duration(64).EUt(16).buildAndRegister();

        RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                .inputs(Mods.Forestry.getItem("refractory_wax"))
                .notConsumable(MetaItems.SHAPE_EXTRUDER_CELL)
                .outputs(Mods.Forestry.getItem("refractory"))
                .duration(128).EUt(16).buildAndRegister();

        // Propolis
        RecipeMaps.CENTRIFUGE_RECIPES.recipeBuilder()
                .inputs(Mods.Forestry.getItem("propolis"))
                .output(MetaItems.STICKY_RESIN)
                .duration(128).EUt(5).buildAndRegister();

        if (Mods.Genetics.isModLoaded()) {

            // DNA Dye
            RecipeMaps.MIXER_RECIPES.recipeBuilder()
                    .input(OrePrefix.dust, Materials.Glowstone)
                    .input("dyePurple")
                    .input("dyeBlue")
                    .outputs(Mods.Genetics.getItem("misc", 1, 8))
                    .duration(100).EUt(GTValues.VA[GTValues.LV]).buildAndRegister();

            // Fluorescent Dye
            RecipeMaps.MIXER_RECIPES.recipeBuilder()
                    .input(OrePrefix.dust, Materials.Glowstone)
                    .input("dyeOrange")
                    .input("dyeYellow")
                    .outputs(Mods.Genetics.getItem("misc", 2, 2))
                    .duration(100).EUt(GTValues.VA[GTValues.LV]).buildAndRegister();

            // Growth Medium
            RecipeMaps.MIXER_RECIPES.recipeBuilder()
                    .input(OrePrefix.dust, Materials.Sugar)
                    .input(OrePrefix.dust, Materials.Bone)
                    .outputs(Mods.Genetics.getItem("misc", 4, 2))
                    .duration(400).EUt(16).buildAndRegister();
        }

        // Random honey stuff
        RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                .notConsumable(MetaItems.SHAPE_MOLD_NUGGET)
                .fluidInputs(Fluids.FOR_HONEY.getFluid(200))
                .outputs(Mods.Forestry.getItem("honey_drop"))
                .duration(400).EUt(7).buildAndRegister();

        RecipeMaps.CENTRIFUGE_RECIPES.recipeBuilder()
                .fluidInputs(Fluids.FOR_HONEY.getFluid(1000))
                .output(OrePrefix.dust, Materials.Sugar, 9)
                .duration(1020).EUt(7).buildAndRegister();
    }

    public static void initRemoval() {
        ModHandler.removeRecipeByName("forestry:sand_to_fertilizer");
        ModHandler.removeRecipeByName("forestry:ash_to_fertilizer");
        if (Mods.MagicBees.isModLoaded()) {
            ModHandler.removeRecipeByName("magicbees:fertilizer1");
            ModHandler.removeRecipeByName("magicbees:fertilizer2");
            ModHandler.removeRecipeByName("magicbees:fertilizer3");
        }
        ModHandler.removeRecipeByName("forestry:wheat_to_compost");
        ModHandler.removeRecipeByName("forestry:ash_to_compost");
        ModHandler.removeRecipeByName("forestry:compost_humus");
        ModHandler.removeRecipeByName("forestry:fertilizer_humus");
        ModHandler.removeRecipeByName("forestry:tin_can");
        ModHandler.removeRecipeByName("forestry:wax_capsule");
        ModHandler.removeRecipeByName("forestry:refractory_capsule");
        if (Mods.Genetics.isModLoaded()) {
            ModHandler.removeRecipeByName("genetics:dna_dye_from_glowstone");
            ModHandler.removeRecipeByName("genetics:dna_dye");
            ModHandler.removeRecipeByName("genetics:fluorescent_dye");
            ModHandler.removeRecipeByName("genetics:growth_medium");
        }
    }
}
