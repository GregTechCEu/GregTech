package gregtech.loaders.recipe.handlers.oreproc;

import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;

import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.MAGNETIC_ORE;
import static gregtech.api.unification.material.info.MaterialFlags.REFINE_BY_SIFTING;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.loaders.recipe.handlers.oreproc.OreRecipeHandler.processMetalSmelting;

public class CrushedRecipeHandler {

    public static void processCrushed(OrePrefix prefix, Material material, OreProperty property) {
        // Get the byproducts used for this step
        Material primaryByproduct = GTUtility.getOrDefault(property.getOreByProducts(), 1, material);
        OrePrefix primaryByproductPrefix = primaryByproduct.hasProperty(PropertyKey.GEM) ? gem : dust;
        int primaryByproductMultiplier = 1;
        if (primaryByproduct.hasProperty(PropertyKey.ORE))
            primaryByproductMultiplier = primaryByproduct.getProperty(PropertyKey.ORE).getOreMultiplier();
        Material secondaryByproduct = GTUtility.getOrDefault(property.getOreByProducts(), 0, material);
        OrePrefix secondaryByproductPrefix = secondaryByproduct.hasProperty(PropertyKey.GEM) ? gem : dust;
        int secondaryByproductMultiplier = 1;
        if (secondaryByproduct.hasProperty(PropertyKey.ORE))
            secondaryByproductMultiplier = secondaryByproduct.getProperty(PropertyKey.ORE).getOreMultiplier();

        // Forge Hammer recipe
        // Crushed Ore -> Impure Dust
        FORGE_HAMMER_RECIPES.recipeBuilder()
                .input(crushed, material)
                .output(dustImpure, material, property.getOreMultiplier())
                .duration(10).EUt(16).buildAndRegister();

        // Macerator recipe
        // Crushed Ore -> Dust (without byproduct)
        MACERATOR_RECIPES.recipeBuilder()
                .input(crushed, material)
                .output(dust, material, property.getOreMultiplier())
                .chancedOutput(dust, Stone, 7500, 0)
                .duration(256).EUt(2).buildAndRegister();

        // Purified Ore -> Refined Ore
        if (material.hasProperty(PropertyKey.GEM)) {
            // Gems go in the Sifter
            SIFTER_RECIPES.recipeBuilder()
                    .input(crushed, material)
                    .output(crushedPurified, material)
                    .chancedOutput(gemFlawless, material, property.getOreMultiplier(), 500, 0)
                    .chancedOutput(gem, material, property.getOreMultiplier(), 1000, 0)
                    .chancedOutput(secondaryByproductPrefix, secondaryByproduct, secondaryByproductMultiplier,2000, 0)
                    .duration(256).EUt(16).buildAndRegister();
        } else if (material.hasFlag(REFINE_BY_SIFTING)) {
            // Certain ores flagged to be in Sifter
            SIFTER_RECIPES.recipeBuilder()
                    .input(crushed, material)
                    .output(crushedPurified, material)
                    .chancedOutput(primaryByproductPrefix, primaryByproduct, primaryByproductMultiplier, 3000, 0)
                    .chancedOutput(secondaryByproductPrefix, secondaryByproduct, secondaryByproductMultiplier,2000, 0)
                    .duration(256).EUt(16).buildAndRegister();
        } else if (material.hasFlag(MAGNETIC_ORE) || primaryByproduct.hasFlag(MAGNETIC_ORE)) {
            // Magnetic Materials or Byproducts go in the Magnetic Separator
            ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder()
                    .input(crushed, material)
                    .output(crushedPurified, material)
                    .chancedOutput(primaryByproductPrefix, primaryByproduct, primaryByproductMultiplier, 3000, 0)
                    .chancedOutput(secondaryByproductPrefix, secondaryByproduct, secondaryByproductMultiplier,2000, 0)
                    .duration(256).EUt(16).buildAndRegister();
        } else {
            // Anything else goes in the Centrifuge
            CENTRIFUGE_RECIPES.recipeBuilder()
                    .input(crushed, material)
                    .output(crushedPurified, material)
                    .chancedOutput(primaryByproductPrefix, primaryByproduct, primaryByproductMultiplier, 2000, 0)
                    .chancedOutput(secondaryByproductPrefix, secondaryByproduct, secondaryByproductMultiplier,2000, 0)
                    .duration(256).EUt(16).buildAndRegister();
        }

        // Hard Hammer crafting recipe
        // Crushed Ore -> Impure Dust
        ModHandler.addShapelessRecipe(String.format("crushed_ore_to_dust_%s", material),
                OreDictUnifier.get(dustImpure, material, property.getOreMultiplier()), 'h', new UnificationEntry(crushed, material));

        processMetalSmelting(prefix, material, property);
    }
}
