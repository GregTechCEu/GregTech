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

import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.loaders.recipe.handlers.oreproc.OreRecipeHandler.processMetalSmelting;

public class CrushedRecipeHandler {

    public static void processCrushed(OrePrefix prefix, Material material, OreProperty property) {
        boolean chancePerTier = ConfigHolder.recipes.oreByproductChancePerTier;
        // Get the byproduct to use for this step
        Material byproduct = GTUtility.getOrDefault(property.getOreByProducts(), 1, material);
        OrePrefix byproductPrefix = byproduct.hasProperty(PropertyKey.GEM) ? gem : dust;
        int byproductMultiplier = 1;
        if (byproduct.hasProperty(PropertyKey.ORE))
            byproductMultiplier = byproduct.getProperty(PropertyKey.ORE).getOreMultiplier();

        // Forge Hammer recipe
        // Crushed Ore -> Impure Dust
        FORGE_HAMMER_RECIPES.recipeBuilder()
                .input(crushed, material)
                .output(dustImpure, material, property.getOreMultiplier())
                .duration(10).EUt(16).buildAndRegister();

        // Macerator recipe
        // Crushed Ore -> Impure Dust (with byproduct)
        MACERATOR_RECIPES.recipeBuilder()
                .input(crushed, material)
                .output(dustImpure, material, property.getOreMultiplier())
                .chancedOutput(byproductPrefix, byproduct, byproductMultiplier, 2000, chancePerTier ? 500 : 0)
                .output(dust, Stone)
                .duration(400).EUt(2).buildAndRegister();

        // Sluice recipe
        // Crushed Ore -> Purified Ore
        SLUICE_RECIPES.recipeBuilder()
                .input(crushed, material)
                .fluidInputs(Water.getFluid(1000))
                .output(crushedPurified, material)
                .chancedOutput(byproductPrefix, byproduct, byproductMultiplier, 1000, 0)
                .output(dust, Stone)
                .fluidOutputs(SluiceJuice.getFluid(1000))
                .duration(400).EUt(16).buildAndRegister();

        // Hard Hammer crafting recipe
        // Crushed Ore -> Impure Dust
        ModHandler.addShapelessRecipe(String.format("crushed_ore_to_dust_%s", material),
                OreDictUnifier.get(dustImpure, material, property.getOreMultiplier()), 'h', new UnificationEntry(crushed, material));

        processMetalSmelting(prefix, material, property);
    }
}
