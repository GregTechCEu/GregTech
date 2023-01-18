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


// NOTES SO FAR
//
// TODO Update the Ore Byproduct Page once things are all finished
// TODO Localization changes for ru_ru and zh_cn for Centrifuged -> Refined
//
// - Durations tweaked:
//     - Initial Macerator step is now 128 * oreMultiplier (excluding the multiplier for nether/end ores)
//     - Subsequent steps are now 128 ticks (down from 400)
//
// - Ores which are not Ingots cannot be direct smelted (use Forge Hammer instead)
// - Direct Smelting Ores no longer uses the Nether/End ore multiplier (still uses the general multiplier)
// - Ore Washer and Thermal Centrifuge steps give 1 Small Dust instead of 3 Tiny Dust
// - Ore Washer Small Dust byproduct is chanced with Water, guaranteed with Distilled Water
// - Direct Smelting fully rebalanced in output amount to make investing in Ore Processing more worthwhile for more than just byproducts
// - Byproduct Multiplier field was removed entirely
public class OreRecipeHandler {

    public static void register() {
        ore.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
        oreEndstone.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
        oreNetherrack.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
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
        crushedPurified.addProcessingHandler(PropertyKey.ORE, PurifiedRecipeHandler::processPurified);
        crushedRefined.addProcessingHandler(PropertyKey.ORE, RefinedRecipeHandler::processRefined);
        dustImpure.addProcessingHandler(PropertyKey.ORE, DustRecipeHandler::processImpure);
        dustPure.addProcessingHandler(PropertyKey.ORE, FlotationRecipeHandler::processFlotation);
        dust.addProcessingHandler(PropertyKey.ORE, DustRecipeHandler::processDust);

        SpecialRecipeHandler.init();
    }

    /**
     * Processing Handler for Ore Blocks
     */
    public static void processOre(OrePrefix prefix, Material material, OreProperty property) {
        boolean chancePerTier = ConfigHolder.recipes.oreByproductChancePerTier;
        // Get the primary byproduct
        Material byproduct = GTUtility.getOrDefault(property.getOreByProducts(), 0, material);
        OrePrefix byproductPrefix = hasGem(byproduct) ? gem : dust;
        int byproductMultiplier = 1;
        if (byproduct.hasProperty(PropertyKey.ORE))
            byproductMultiplier = byproduct.getProperty(PropertyKey.ORE).getOreMultiplier();

        // Calculate the base crushed output depending on the ore type
        int oreTypeMultiplier = (int) (prefix.getMaterialAmount(material) / M);

        // Forge Hammer recipe. Outputs a Gem if possible
        if (hasGem(material)) {
            FORGE_HAMMER_RECIPES.recipeBuilder()
                    .input(prefix, material)
                    .output(gem, material, property.getOreMultiplier() * oreTypeMultiplier)
                    .duration(10).EUt(16)
                    .buildAndRegister();
        } else {
            FORGE_HAMMER_RECIPES.recipeBuilder()
                    .input(prefix, material)
                    .output(crushed, material, oreTypeMultiplier)
                    .duration(10).EUt(16)
                    .buildAndRegister();
        }

        // Get the stone type dust stack, the 3rd output of the recipe
        ItemStack stoneTypeDust = OreDictUnifier.getDust(prefix.secondaryMaterials.get(0));

        // Macerator recipe
        MACERATOR_RECIPES.recipeBuilder()
                .input(prefix, material)
                .output(crushed, material, 2 * oreTypeMultiplier)
                .chancedOutput(byproductPrefix, byproduct, byproductMultiplier * oreTypeMultiplier, 2000, chancePerTier ? 500 : 0)
                .chancedOutput(stoneTypeDust, 6000, chancePerTier ? 1000 : 0)
                .duration(400 * oreTypeMultiplier).EUt(2)
                .buildAndRegister();

        // Smelting recipe
        processMetalSmelting(prefix, material, property);
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
     *
     * Allowed Prefixes and their outputs for non-elements:
     * - Ore Block:   1 Ingot * the passed inputPrefix' material amount multiplier (End & Nether Ores are M * 2)
     * - Impure Dust: 1 Ingot
     * - Pure Dust:   1 Ingot
     * - Dust:        1 Ingot
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
