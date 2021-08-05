package gregtech.loaders.oreprocessing;

import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;

import static gregtech.api.unification.material.info.MaterialFlags.HIGH_SIFTER_OUTPUT;

public class OreRecipeHandler {

    public static void register() {
        OrePrefix.ore.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
        OrePrefix.oreGranite.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
        OrePrefix.oreDiorite.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
        OrePrefix.oreAndesite.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
        OrePrefix.oreBasalt.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
        OrePrefix.oreBlackgranite.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
        OrePrefix.oreEndstone.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
        OrePrefix.oreGravel.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
        OrePrefix.oreNetherrack.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
        OrePrefix.oreMarble.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
        OrePrefix.oreRedgranite.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
        OrePrefix.oreSand.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
        OrePrefix.oreRedSand.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);

        OrePrefix.crushed.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processCrushedOre);
        OrePrefix.crushedPurified.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processCrushedPurified);
        OrePrefix.crushedCentrifuged.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processCrushedCentrifuged);
        OrePrefix.dustImpure.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processDirtyDust);
        OrePrefix.dustPure.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processPureDust);
        OrePrefix.dust.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processCleanDust);
    }


    private static void processMetalSmelting(OrePrefix crushedPrefix, Material material, OreProperty property) {
        Material smeltingResult = property.getDirectSmeltResult() != null ? property.getDirectSmeltResult() : material;

        if (smeltingResult.hasProperty(PropertyKey.INGOT)) {
            ItemStack ingotStack = OreDictUnifier.get(OrePrefix.ingot, smeltingResult);

            if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingResult)) {
                ModHandler.addSmeltingRecipe(new UnificationEntry(crushedPrefix, material), ingotStack);
            }
        }
    }

    public static void processOre(OrePrefix orePrefix, Material material, OreProperty property) {
        Material byproductMaterial = GTUtility.selectItemInList(0, material, property.getOreByProducts(), Material.class);
        ItemStack byproductStack = OreDictUnifier.get(OrePrefix.dust, byproductMaterial);
        ItemStack crushedStack = OreDictUnifier.get(OrePrefix.crushed, material);
        ItemStack ingotStack;
        Material smeltingMaterial = property.getDirectSmeltResult() == null ? material : property.getDirectSmeltResult();
        double amountOfCrushedOre = property.getOreMultiplier();
        if (smeltingMaterial.hasProperty(PropertyKey.INGOT)) {
            ingotStack = OreDictUnifier.get(OrePrefix.ingot, smeltingMaterial);
        } else if (smeltingMaterial.hasProperty(PropertyKey.GEM)) {
            ingotStack = OreDictUnifier.get(OrePrefix.gem, smeltingMaterial);
        } else {
            ingotStack = OreDictUnifier.get(OrePrefix.dust, smeltingMaterial);
        }
        ingotStack.setCount(ingotStack.getCount() * property.getOreMultiplier());
        crushedStack.setCount(crushedStack.getCount() * property.getOreMultiplier());

        if (!crushedStack.isEmpty()) {
            RecipeMaps.FORGE_HAMMER_RECIPES.recipeBuilder()
                .input(orePrefix, material)
                .outputs(GTUtility.copyAmount((int) Math.ceil(amountOfCrushedOre), crushedStack))
                .duration(100).EUt(6)
                .buildAndRegister();

            RecipeBuilder<?> builder = RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .input(orePrefix, material)
                .outputs(GTUtility.copyAmount((int) Math.round(amountOfCrushedOre) * 2, crushedStack))
                .chancedOutput(byproductStack, 1400, 850)
                .duration(400).EUt(12);
            for (MaterialStack secondaryMaterial : orePrefix.secondaryMaterials) {
                if (secondaryMaterial.material.hasProperty(PropertyKey.DUST)) {
                    ItemStack dustStack = OreDictUnifier.getDust(secondaryMaterial);
                    builder.chancedOutput(dustStack, 6700, 800);
                }
            }
            builder.buildAndRegister();
        }

        //do not try to add smelting recipes for materials which require blast furnace
        if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingMaterial)) {
            ModHandler.addSmeltingRecipe(new UnificationEntry(orePrefix, material), ingotStack);
        }
    }

    public static void processCrushedOre(OrePrefix crushedPrefix, Material material, OreProperty property) {
        ItemStack impureDustStack = OreDictUnifier.get(OrePrefix.dustImpure, material);
        // TODO RIGHT HERE. JEI Page will need to be updated for this
        Material byproductMaterial = GTUtility.selectItemInList(0, material, property.getOreByProducts(), Material.class);

        //fallback for dirtyGravel, shard & clump
        if (impureDustStack.isEmpty()) {
            impureDustStack = GTUtility.copy(
                OreDictUnifier.get(OrePrefix.dirtyGravel, material),
                OreDictUnifier.get(OrePrefix.shard, material),
                OreDictUnifier.get(OrePrefix.clump, material),
                OreDictUnifier.get(OrePrefix.dust, material));
        }

        RecipeMaps.FORGE_HAMMER_RECIPES.recipeBuilder()
            .input(crushedPrefix, material)
            .outputs(impureDustStack)
            .duration(60).EUt(8)
            .buildAndRegister();

        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
            .input(crushedPrefix, material)
            .outputs(impureDustStack)
            .duration(200).EUt(12)
            .chancedOutput(OreDictUnifier.get(OrePrefix.dust, byproductMaterial, property.getByProductMultiplier()), 1400, 850)
            .buildAndRegister();

        ItemStack crushedPurifiedOre = GTUtility.copy(
            OreDictUnifier.get(OrePrefix.crushedPurified, material),
            OreDictUnifier.get(OrePrefix.dust, material));
        ItemStack crushedCentrifugedOre = GTUtility.copy(
            OreDictUnifier.get(OrePrefix.crushedCentrifuged, material),
            OreDictUnifier.get(OrePrefix.dust, material));

        RecipeMaps.ORE_WASHER_RECIPES.recipeBuilder()
            .input(crushedPrefix, material)
            .fluidInputs(ModHandler.getWater(1000))
            .outputs(crushedPurifiedOre,
                OreDictUnifier.get(OrePrefix.dustTiny, byproductMaterial, 3),
                OreDictUnifier.get(OrePrefix.dust, Materials.Stone))
            .buildAndRegister();

        RecipeMaps.ORE_WASHER_RECIPES.recipeBuilder()
            .input(crushedPrefix, material)
            .fluidInputs(ModHandler.getDistilledWater(1000))
            .outputs(crushedPurifiedOre,
                OreDictUnifier.get(OrePrefix.dustTiny, byproductMaterial, 3),
                OreDictUnifier.get(OrePrefix.dust, Materials.Stone))
            .duration(300)
            .buildAndRegister();

        RecipeMaps.THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
            .input(crushedPrefix, material)
            .duration((int) material.getAverageMass() * 20)
            .outputs(crushedCentrifugedOre,
                OreDictUnifier.get(OrePrefix.dustTiny, byproductMaterial, property.getByProductMultiplier() * 3),
                OreDictUnifier.get(OrePrefix.dust, Materials.Stone))
            .buildAndRegister();

        if (property.getWashedIn() != null) {
            Material washingByproduct = GTUtility.selectItemInList(3, material, property.getOreByProducts(), Material.class);
            RecipeMaps.CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedPrefix, material)
                .fluidInputs(property.getWashedIn().getFluid(property.getWashedIn() == Materials.SodiumPersulfate ? 100 : 1000))
                .outputs(crushedPurifiedOre)
                .chancedOutput(OreDictUnifier.get(OrePrefix.dust, washingByproduct, property.getByProductMultiplier()), 7000, 580)
                .chancedOutput(OreDictUnifier.get(OrePrefix.dust, Materials.Stone), 4000, 650)
                .duration(800).EUt(8)
                .buildAndRegister();
        }

        ModHandler.addShapelessRecipe(String.format("crushed_ore_to_dust_%s", material),
            impureDustStack, 'h', new UnificationEntry(crushedPrefix, material));

        processMetalSmelting(crushedPrefix, material, property);
    }

    public static void processCrushedCentrifuged(OrePrefix centrifugedPrefix, Material material, OreProperty property) {
        ItemStack dustStack = OreDictUnifier.get(OrePrefix.dust, material);
        ItemStack byproductStack = OreDictUnifier.get(OrePrefix.dust, GTUtility.selectItemInList(2,
            material, property.getOreByProducts(), Material.class), 1);

        RecipeMaps.FORGE_HAMMER_RECIPES.recipeBuilder()
            .input(centrifugedPrefix, material)
            .outputs(dustStack)
            .duration(60).EUt(8)
            .buildAndRegister();

        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
            .input(centrifugedPrefix, material)
            .outputs(dustStack)
            .chancedOutput(byproductStack, 1400, 850)
            .duration(200).EUt(12)
            .buildAndRegister();

        ModHandler.addShapelessRecipe(String.format("centrifuged_ore_to_dust_%s", material), dustStack,
            'h', new UnificationEntry(centrifugedPrefix, material));

        processMetalSmelting(centrifugedPrefix, material, property);
    }

    public static void processCrushedPurified(OrePrefix purifiedPrefix, Material material, OreProperty property) {
        ItemStack crushedCentrifugedStack = OreDictUnifier.get(OrePrefix.crushedCentrifuged, material);
        ItemStack dustStack = OreDictUnifier.get(OrePrefix.dustPure, material);
        Material byproductMaterial = GTUtility.selectItemInList(
                1, material, property.getOreByProducts(), Material.class);
        ItemStack byproductStack = OreDictUnifier.get(OrePrefix.dust, byproductMaterial);

        RecipeMaps.FORGE_HAMMER_RECIPES.recipeBuilder()
            .input(purifiedPrefix, material)
            .outputs(dustStack)
            .duration(60)
            .EUt(8)
            .buildAndRegister();

        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
            .input(purifiedPrefix, material)
            .outputs(dustStack)
            .chancedOutput(byproductStack, 1400, 850)
            .duration(200)
            .EUt(12)
            .buildAndRegister();

        ModHandler.addShapelessRecipe(String.format("purified_ore_to_dust_%s", material), dustStack,
            'h', new UnificationEntry(purifiedPrefix, material));

        if (!crushedCentrifugedStack.isEmpty()) {
            RecipeMaps.THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
                .input(purifiedPrefix, material)
                .outputs(crushedCentrifugedStack, OreDictUnifier.get(OrePrefix.dustTiny, byproductMaterial, 3))
                .duration((int) (material.getAverageMass() * 20))
                .EUt(60)
                .buildAndRegister();
        }

        if (material.hasProperty(PropertyKey.GEM)) {
            ItemStack exquisiteStack = OreDictUnifier.get(OrePrefix.gemExquisite, material);
            ItemStack flawlessStack = OreDictUnifier.get(OrePrefix.gemFlawless, material);
            ItemStack gemStack = OreDictUnifier.get(OrePrefix.gem, material);
            ItemStack flawedStack = OreDictUnifier.get(OrePrefix.gemFlawed, material);
            ItemStack chippedStack = OreDictUnifier.get(OrePrefix.gemChipped, material);

            if (material.hasFlag(HIGH_SIFTER_OUTPUT)) {
                RecipeMaps.SIFTER_RECIPES.recipeBuilder()
                    .input(purifiedPrefix, material)
                    .chancedOutput(exquisiteStack, 300, 60)
                    .chancedOutput(flawlessStack, 1200, 180)
                    .chancedOutput(gemStack, 4500, 540)
                    .chancedOutput(flawedStack, 1400, 240)
                    .chancedOutput(chippedStack, 2800, 320)
                    .chancedOutput(dustStack, 3500, 500)
                    .duration(800).EUt(16)
                    .buildAndRegister();
            } else {
                RecipeMaps.SIFTER_RECIPES.recipeBuilder()
                    .input(purifiedPrefix, material)
                    .chancedOutput(exquisiteStack, 100, 30)
                    .chancedOutput(flawlessStack, 400, 70)
                    .chancedOutput(gemStack, 1500, 300)
                    .chancedOutput(flawedStack, 2000, 240)
                    .chancedOutput(chippedStack, 4000, 320)
                    .chancedOutput(dustStack, 5000, 600)
                    .duration(800).EUt(16)
                    .buildAndRegister();
            }
        }
        processMetalSmelting(purifiedPrefix, material, property);
    }

    public static void processDirtyDust(OrePrefix dustPrefix, Material material, OreProperty property) {
        ItemStack dustStack = OreDictUnifier.get(OrePrefix.dust, material);

        Material byproduct = GTUtility.selectItemInList(
                2, material, property.getOreByProducts(), Material.class);

        RecipeBuilder<?> builder = RecipeMaps.CENTRIFUGE_RECIPES.recipeBuilder()
            .input(dustPrefix, material)
            .outputs(dustStack)
            .duration((int) (material.getAverageMass() * 4)).EUt(24);

        if (byproduct.hasProperty(PropertyKey.DUST)) {
            builder.outputs(OreDictUnifier.get(OrePrefix.dustTiny, byproduct, 3));
        } else {
            builder.fluidOutputs(byproduct.getFluid(GTValues.L / 9));
        }

        builder.buildAndRegister();

        //dust gains same amount of material as normal dust
        processMetalSmelting(dustPrefix, material, property);
    }

    public static void processPureDust(OrePrefix purePrefix, Material material, OreProperty property) {
        Material byproductMaterial = GTUtility.selectItemInList(
                1, material, property.getOreByProducts(), Material.class);
        ItemStack dustStack = OreDictUnifier.get(OrePrefix.dust, material);

        if (property.getSeparatedInto() != null) {
            ItemStack separatedStack = OreDictUnifier.get(OrePrefix.dustSmall, property.getSeparatedInto());
            RecipeMaps.ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder()
                    .input(purePrefix, material)
                    .outputs(dustStack)
                    .chancedOutput(separatedStack, 4000, 850)
                    .duration((int) property.getSeparatedInto().getAverageMass()).EUt(24)
                    .buildAndRegister();
        }

        if (dustStack.isEmpty()) {
            //fallback for reduced & cleanGravel
            dustStack = GTUtility.copy(
                OreDictUnifier.get(OrePrefix.reduced, material),
                OreDictUnifier.get(OrePrefix.cleanGravel, material));
        }

        RecipeMaps.CENTRIFUGE_RECIPES.recipeBuilder()
            .input(purePrefix, material)
            .outputs(dustStack, OreDictUnifier.get(OrePrefix.dustTiny, byproductMaterial, 3))
            .duration((int) (material.getAverageMass() * 4))
            .EUt(5)
            .buildAndRegister();

        processMetalSmelting(purePrefix, material, property);
    }

    public static void processCleanDust(OrePrefix dustPrefix, Material material, OreProperty property) {
        processMetalSmelting(dustPrefix, material, property);
    }

    private static boolean doesMaterialUseNormalFurnace(Material material) {
        return !material.hasProperty(PropertyKey.BLAST);
    }

}
