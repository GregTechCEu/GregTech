package gregtech.loaders.recipe.handlers.oreproc;

import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTLog;
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
        crushedRefined.addProcessingHandler(PropertyKey.ORE, PurifiedRecipeHandler::processRefined);
        dustImpure.addProcessingHandler(PropertyKey.ORE, DustRecipeHandler::processImpure);
        dust.addProcessingHandler(PropertyKey.ORE, DustRecipeHandler::processDust);

        SpecialRecipeHandler.init();
    }

    /**
     * Processing Handler for Ore Blocks
     */
    public static void processOre(OrePrefix prefix, Material material, OreProperty property) {
        // Get the primary byproduct, the secondary output of the recipe. Prioritize Gem if possible
        Material byproductMaterial = GTUtility.selectItemInList(0, material, property.getOreByProducts(), Material.class);
        ItemStack byproductStack = OreDictUnifier.get(gem, byproductMaterial);
        if (byproductStack.isEmpty()) byproductStack = OreDictUnifier.get(dust, byproductMaterial);

        // Get the stone type dust stack, the 3rd output of the recipe
        ItemStack stoneTypeDust = OreDictUnifier.getDust(prefix.secondaryMaterials.get(0));

        // Calculate the base crushed output.
        // Combines the Ore Multiplier, as well as the bonus crushed from certain ore types
        int baseOutputAmount = property.getOreMultiplier();
        int oreTypeMultiplier = (int) (prefix.getMaterialAmount(material) / M);

        // Forge Hammer recipe. Outputs a Gem if possible
        FORGE_HAMMER_RECIPES.recipeBuilder()
                .input(prefix, material)
                .output(hasGem(material) ? gem : crushed, material, baseOutputAmount * oreTypeMultiplier)
                .duration(10).EUt(16)
                .buildAndRegister();

        // Macerator recipe
        MACERATOR_RECIPES.recipeBuilder()
                .input(prefix, material)
                .output(crushed, material, baseOutputAmount * oreTypeMultiplier * 2)
                .chancedOutput(byproductStack, 1400, 850)
                .chancedOutput(stoneTypeDust, 6700, 800)
                .duration(400).EUt(2)
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
        return material.hasProperty(PropertyKey.GEM) && !gem.isIgnored(material);
    }

    static boolean hasPrintedName = false;
    /**
     * Adds a Smelting Recipe to the ItemStack generated from OreDictUnifier.get(inputPrefix, material), or the direct smelting result override.
     * Only adds this recipe IF:
     * - The Direct-Smelt result is an Ingot
     * - This Material does not have an EBF Temperature
     *
     * Allowed Prefixes and their outputs for non-elements:
     * - Ore Block:   1 Ingot * the passed OreProperty's Ore Multiplier, IF the passed Material is an Element
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

        if (material == Materials.Magnetite) {
            if (!hasPrintedName) {
                GTLog.logger.info("======================================");
                GTLog.logger.info("Material: {}", material);
                GTLog.logger.info("Ore Multiplier: {}", property.getOreMultiplier());
                GTLog.logger.info("======================================");
                hasPrintedName = true;
            }
            GTLog.logger.info("Ore Prefix: {}", inputPrefix.name);
            GTLog.logger.info("Amount Output: {}", 1.0 * amountOutput / M);
            GTLog.logger.info("Material Amount: M * {}", inputPrefix.getMaterialAmount(material) * (1.0 / M));
            GTLog.logger.info("Num Components of {}: {}", smeltingResult, material.getNumComponentsOf(smeltingResult));
            GTLog.logger.info("Num Components: {}", material.getNumComponents());
            GTLog.logger.info("======================================");
        }

        ItemStack outputStack = OreDictUnifier.getIngot(smeltingResult, amountOutput * getSmeltMultiplier(inputPrefix, property));
        if (!outputStack.isEmpty()) {
            ModHandler.addSmeltingRecipe(new UnificationEntry(inputPrefix, material), outputStack, 0.5f);
        }
    }

    private static final String ORE = "ore";
    protected static int getSmeltMultiplier(OrePrefix prefix, OreProperty property) {
        return prefix.name().startsWith(ORE) ? property.getOreMultiplier() : 1;
    }
}
