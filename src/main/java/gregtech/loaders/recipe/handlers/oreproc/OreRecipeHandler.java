package gregtech.loaders.recipe.handlers.oreproc;

import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.M;
import static gregtech.api.recipes.RecipeMaps.FORGE_HAMMER_RECIPES;
import static gregtech.api.recipes.RecipeMaps.MACERATOR_RECIPES;
import static gregtech.api.unification.ore.OrePrefix.*;

// TODO Update the Ore Byproduct Page once things are all finished

public class OreRecipeHandler {

    public static void register() {
        ore.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
        oreNetherrack.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
        oreEndstone.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
        if (ConfigHolder.worldgen.allUniqueStoneTypes) {
            oreGranite.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
            oreDiorite.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
            oreAndesite.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
            oreBasalt.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
            oreBlackgranite.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
            oreMarble.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
            oreRedgranite.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
            oreSand.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
            oreRedSand.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
        }
        crushed.addProcessingHandler(PropertyKey.ORE, CrushedRecipeHandler::processCrushed);
        washed.addProcessingHandler(PropertyKey.ORE, WashedRecipeHandler::processWashed);
        purified.addProcessingHandler(PropertyKey.ORE, PurifiedRecipeHandler::processPurified);
        refined.addProcessingHandler(PropertyKey.ORE, RefinedRecipeHandler::processRefined);
        dust.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processMetalSmelting);

        SpecialRecipeHandler.init();
    }

    /**
     * Processing Handler for Ore Blocks
     */
    public static void processOre(OrePrefix prefix, Material material, OreProperty property) {
        // Get the primary byproduct
        Material byproduct = GTUtility.getOrDefault(property.getOreByProducts(), 0, material);
        OrePrefix byproductPrefix = hasGem(byproduct) ? gem : dust;
        int byproductMultiplier = 1;
        if (byproduct.hasProperty(PropertyKey.ORE))
            byproductMultiplier = byproduct.getProperty(PropertyKey.ORE).getOreMultiplier();

        // Calculate the base crushed output depending on the ore type
        int oreTypeMultiplier = (int) (prefix.getMaterialAmount(material) / M);

        // Hard Hammer crafting recipe. Outputs a Gem if possible
        if(hasGem(material)) {
            ModHandler.addShapelessRecipe(String.format(prefix.name().toLowerCase() + "_to_gem_%s", material),
                    OreDictUnifier.get(gem, material, property.getOreMultiplier()), 'h', new UnificationEntry(prefix, material));
        } else {
            ModHandler.addShapelessRecipe(String.format(prefix.name().toLowerCase() + "_to_dust_%s", material),
                    OreDictUnifier.get(dust, material, property.getOreMultiplier()), 'h', new UnificationEntry(prefix, material));
        }

        // Forge Hammer recipe
        if (hasGem(material)) {
            FORGE_HAMMER_RECIPES.recipeBuilder()
                    .input(prefix, material)
                    .output(gem, material, property.getOreMultiplier() * oreTypeMultiplier)
                    .duration(10).EUt(16)
                    .buildAndRegister();
        } else {
            FORGE_HAMMER_RECIPES.recipeBuilder()
                    .input(prefix, material)
                    .output(dust, material, property.getOreMultiplier() * oreTypeMultiplier)
                    .duration(10).EUt(16)
                    .buildAndRegister();
        }

        // Get the stone type dust stack, the 3rd output of the recipe
        ItemStack stoneTypeDust = OreDictUnifier.getDust(prefix.secondaryMaterials.get(0));

        // Macerator recipe
        MACERATOR_RECIPES.recipeBuilder()
                .input(prefix, material)
                .output(crushed, material, oreTypeMultiplier)
                .chancedOutput(byproductPrefix, byproduct, byproductMultiplier * oreTypeMultiplier, 2000, 0)
                .chancedOutput(stoneTypeDust, 5000, 0)
                .duration(200 * oreTypeMultiplier).EUt(2)
                .buildAndRegister();
    }

    ////////////////////////////////////////
    // Helpers to be used in this package //
    ////////////////////////////////////////

    protected static boolean doesMaterialUseNormalFurnace(Material material) {
        return !material.hasProperty(PropertyKey.BLAST);
    }

    protected static boolean hasGem(Material material) {
        return material.hasProperty(PropertyKey.GEM);
    }

    /**
     * Adds a Smelting Recipe to the ItemStack generated from OreDictUnifier.get(inputPrefix, material), or the direct smelting result override.
     * Only adds this recipe IF:
     * - The Direct-Smelt result is an Ingot
     * - This Material does not have an EBF Temperature
     */
    protected static void processMetalSmelting(OrePrefix inputPrefix, Material material, OreProperty property) {
        // Get the Material that should be output by smelting this Item
        // Exit early if the result material has an EBF temperature, or is not an Ingot
        Material smeltingResult = property.getDirectSmeltResult() != null ? property.getDirectSmeltResult() : material;
        if (!doesMaterialUseNormalFurnace(smeltingResult) || !smeltingResult.hasProperty(PropertyKey.INGOT)) return;

        long amountOutput = ConfigHolder.recipes.harderOreProcessing
                // Output prefix amount times the ratio of the direct smelt output to the total component list.
                // For example, Chalcopyrite (CuFeS2) would return `M * 1 / 4`.
                ? inputPrefix.getMaterialAmount(material) * material.getNumComponentsOf(smeltingResult) / material.getNumComponents()

                // In the easy mode, always return prefix amount here and do not check for the components.
                : inputPrefix.getMaterialAmount(material);

        ItemStack outputStack = OreDictUnifier.getIngot(smeltingResult, amountOutput);
        if (!outputStack.isEmpty()) {
            ModHandler.addSmeltingRecipe(new UnificationEntry(inputPrefix, material), outputStack, 0.5f);
        }
    }
}
