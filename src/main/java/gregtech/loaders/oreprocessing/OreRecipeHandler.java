package gregtech.loaders.oreprocessing;

import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.type.*;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;

public class OreRecipeHandler {

    public static void register() {
        OrePrefix.ore.addProcessingHandler(DustMaterial.class, OreRecipeHandler::processOre);
        OrePrefix.oreBasalt.addProcessingHandler(DustMaterial.class, OreRecipeHandler::processOre);
        OrePrefix.oreBlackgranite.addProcessingHandler(DustMaterial.class, OreRecipeHandler::processOre);
        OrePrefix.oreEndstone.addProcessingHandler(DustMaterial.class, OreRecipeHandler::processOre);
        OrePrefix.oreGravel.addProcessingHandler(DustMaterial.class, OreRecipeHandler::processOre);
        OrePrefix.oreNetherrack.addProcessingHandler(DustMaterial.class, OreRecipeHandler::processOre);
        OrePrefix.oreMarble.addProcessingHandler(DustMaterial.class, OreRecipeHandler::processOre);
        OrePrefix.oreRedgranite.addProcessingHandler(DustMaterial.class, OreRecipeHandler::processOre);
        OrePrefix.oreSand.addProcessingHandler(DustMaterial.class, OreRecipeHandler::processOre);

        OrePrefix.crushed.addProcessingHandler(DustMaterial.class, OreRecipeHandler::processCrushedOre);
        OrePrefix.crushedPurified.addProcessingHandler(DustMaterial.class, OreRecipeHandler::processCrushedPurified);
        OrePrefix.crushedCentrifuged.addProcessingHandler(DustMaterial.class, OreRecipeHandler::processCrushedCentrifuged);
        OrePrefix.crystalline.addProcessingHandler(SolidMaterial.class, OreRecipeHandler::processCrystallizedPurified);
        OrePrefix.dustImpure.addProcessingHandler(DustMaterial.class, OreRecipeHandler::processDirtyDust);
        OrePrefix.dustPure.addProcessingHandler(DustMaterial.class, OreRecipeHandler::processPureDust);
        OrePrefix.dust.addProcessingHandler(DustMaterial.class, OreRecipeHandler::processCleanDust);
    }


    private static void processMetalSmelting(OrePrefix crushedPrefix, DustMaterial material) {
        DustMaterial smeltingMaterial = material.directSmelting == null ? material : material.directSmelting;
        if (smeltingMaterial instanceof IngotMaterial) {
            ItemStack ingotStack = OreDictUnifier.get(OrePrefix.ingot, smeltingMaterial);

            if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingMaterial)) {
                ModHandler.addSmeltingRecipe(new UnificationEntry(crushedPrefix, material), ingotStack);
            }
        }
    }

    public static void processOre(OrePrefix orePrefix, DustMaterial material) {
        DustMaterial byproductMaterial = GTUtility.selectItemInList(0, material, material.oreByProducts, DustMaterial.class);
        ItemStack byproductStack = OreDictUnifier.get(OrePrefix.dust, byproductMaterial);
        ItemStack crushedStack = OreDictUnifier.get(OrePrefix.crushed, material.crushedInto);
        ItemStack ingotStack;
        DustMaterial smeltingMaterial = material.directSmelting == null ? material : material.directSmelting;
        double amountOfCrushedOre = material.oreMultiplier;
        if (smeltingMaterial instanceof IngotMaterial) {
            ingotStack = OreDictUnifier.get(OrePrefix.ingot, smeltingMaterial);
        } else if (smeltingMaterial instanceof GemMaterial) {
            ingotStack = OreDictUnifier.get(OrePrefix.gem, smeltingMaterial);
        } else {
            ingotStack = OreDictUnifier.get(OrePrefix.dust, smeltingMaterial);
        }
        ingotStack.setCount(ingotStack.getCount() * material.oreMultiplier);
        crushedStack.setCount(crushedStack.getCount() * material.oreMultiplier);

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
                if (secondaryMaterial.material instanceof DustMaterial) {
                    ItemStack dustStack = OreDictUnifier.getDust(secondaryMaterial);
                    builder.chancedOutput(dustStack, 6700, 800);
                }
            }
            builder.buildAndRegister();
        }

        //do not try to add smelting recipes for materials which require blast furnace
        if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingMaterial) && !material.disableDirectSmelting) {
            ModHandler.addSmeltingRecipe(new UnificationEntry(orePrefix, material), ingotStack);
        }
    }

    public static void processCrushedOre(OrePrefix crushedPrefix, DustMaterial material) {
        ItemStack impureDustStack = OreDictUnifier.get(OrePrefix.dustImpure, material);
        // TODO RIGHT HERE. JEI Page will need to be updated for this
        DustMaterial byproductMaterial = GTUtility.selectItemInList(0, material, material.oreByProducts, DustMaterial.class);

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
            .chancedOutput(OreDictUnifier.get(OrePrefix.dust, byproductMaterial, material.byProductMultiplier), 1400, 850)
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
                OreDictUnifier.get(OrePrefix.dustTiny, byproductMaterial, material.byProductMultiplier * 3),
                OreDictUnifier.get(OrePrefix.dust, Materials.Stone))
            .buildAndRegister();

        if (material.washedIn != null) {
            DustMaterial washingByproduct = GTUtility.selectItemInList(3, material, material.oreByProducts, DustMaterial.class);
            RecipeMaps.CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedPrefix, material)
                .fluidInputs(material.washedIn.getFluid(material.washedIn == Materials.SodiumPersulfate ? 100 : 1000))
                .outputs(crushedPurifiedOre)
                .chancedOutput(OreDictUnifier.get(OrePrefix.dust, washingByproduct, material.byProductMultiplier), 7000, 580)
                .chancedOutput(OreDictUnifier.get(OrePrefix.dust, Materials.Stone), 4000, 650)
                .duration(800).EUt(8)
                .buildAndRegister();
        }

        ModHandler.addShapelessRecipe(String.format("crushed_ore_to_dust_%s", material),
            impureDustStack, 'h', new UnificationEntry(crushedPrefix, material));

        processMetalSmelting(crushedPrefix, material);
    }

    public static void processCrushedCentrifuged(OrePrefix centrifugedPrefix, DustMaterial material) {
        ItemStack dustStack = OreDictUnifier.get(OrePrefix.dust, material);
        ItemStack byproductStack = OreDictUnifier.get(OrePrefix.dust, GTUtility.selectItemInList(2,
            material, material.oreByProducts, DustMaterial.class), 1);

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

        processMetalSmelting(centrifugedPrefix, material);
    }

    public static void processCrushedPurified(OrePrefix purifiedPrefix, DustMaterial material) {
        ItemStack crushedCentrifugedStack = OreDictUnifier.get(OrePrefix.crushedCentrifuged, material);
        ItemStack dustStack = OreDictUnifier.get(OrePrefix.dustPure, material);
        Material byproductMaterial = GTUtility.selectItemInList(1, material, material.oreByProducts, DustMaterial.class);
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

        if (material instanceof GemMaterial) {
            ItemStack exquisiteStack = OreDictUnifier.get(OrePrefix.gemExquisite, material);
            ItemStack flawlessStack = OreDictUnifier.get(OrePrefix.gemFlawless, material);
            ItemStack gemStack = OreDictUnifier.get(OrePrefix.gem, material);
            ItemStack flawedStack = OreDictUnifier.get(OrePrefix.gemFlawed, material);
            ItemStack chippedStack = OreDictUnifier.get(OrePrefix.gemChipped, material);

            if (material.hasFlag(GemMaterial.MatFlags.HIGH_SIFTER_OUTPUT)) {
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
        processMetalSmelting(purifiedPrefix, material);
    }

    public static void processDirtyDust(OrePrefix dustPrefix, DustMaterial material) {
        ItemStack dustStack = OreDictUnifier.get(OrePrefix.dust, material);
        if (dustPrefix == OrePrefix.dustPure && material.separatedOnto != null) {
            ItemStack separatedStack = OreDictUnifier.get(OrePrefix.dustSmall, material.separatedOnto);
            RecipeMaps.ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder()
                .input(dustPrefix, material)
                .outputs(dustStack)
                .chancedOutput(separatedStack, 4000, 850)
                .duration((int) material.separatedOnto.getAverageMass()).EUt(24)
                .buildAndRegister();
        }

        FluidMaterial byproduct = GTUtility.selectItemInList(2, material, material.oreByProducts, FluidMaterial.class);

        RecipeBuilder<?> builder = RecipeMaps.CENTRIFUGE_RECIPES.recipeBuilder()
            .input(dustPrefix, material)
            .outputs(dustStack)
            .duration((int) (material.getAverageMass() * 4)).EUt(24);

        if (byproduct instanceof DustMaterial) {
            builder.outputs(OreDictUnifier.get(OrePrefix.dustTiny, byproduct, 3));
        } else {
            builder.fluidOutputs(byproduct.getFluid(GTValues.L / 9));
        }

        builder.buildAndRegister();

        //dust gains same amount of material as normal dust
        processMetalSmelting(dustPrefix, material);
    }

    public static void processPureDust(OrePrefix purePrefix, DustMaterial material) {
        DustMaterial byproductMaterial = GTUtility.selectItemInList(1, material, material.oreByProducts, DustMaterial.class);
        ItemStack dustStack = OreDictUnifier.get(OrePrefix.dust, material);

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

        processMetalSmelting(purePrefix, material);
    }


    public static void processCrystallizedPurified(OrePrefix crystallizedPrefix, SolidMaterial material) {
        DustMaterial resultMaterial = material.macerateInto == null ? material : material.macerateInto;
        ItemStack dustStack = OreDictUnifier.get(OrePrefix.dust, resultMaterial);
        RecipeMaps.FORGE_HAMMER_RECIPES.recipeBuilder()
            .input(crystallizedPrefix, material)
            .outputs(dustStack)
            .duration(10)
            .EUt(10)
            .buildAndRegister();

        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
            .input(crystallizedPrefix, material)
            .outputs(dustStack)
            .duration(20)
            .EUt(16)
            .buildAndRegister();

        processMetalSmelting(crystallizedPrefix, material);
    }

    public static void processCleanDust(OrePrefix dustPrefix, DustMaterial material) {
        processMetalSmelting(dustPrefix, material);
    }

    private static boolean doesMaterialUseNormalFurnace(Material material) {
        return !(material instanceof IngotMaterial) ||
            ((IngotMaterial) material).blastFurnaceTemperature <= 0;
    }

}
