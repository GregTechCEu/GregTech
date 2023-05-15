package gregtech.loaders.recipe.handlers.oreproc;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;

import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class WashedRecipeHandler {

    public static void processWashed(OrePrefix prefix, Material material, OreProperty property) {
        // Get the byproducts used for this step
        Material primaryByproduct = GTUtility.getOrDefault(property.getOreByProducts(), 1, material);
        OrePrefix primaryByproductPrefix = primaryByproduct.hasProperty(PropertyKey.GEM) ? gem : dust;
        int primaryByproductMultiplier = 1;
        if (primaryByproduct.hasProperty(PropertyKey.ORE))
            primaryByproductMultiplier = primaryByproduct.getProperty(PropertyKey.ORE).getOreMultiplier();

        Material secondaryByproduct = GTUtility.getOrDefault(property.getOreByProducts(), 2, material);
        OrePrefix secondaryByproductPrefix = secondaryByproduct.hasProperty(PropertyKey.GEM) ? gem : dust;
        int secondaryByproductMultiplier = 1;
        if (secondaryByproduct.hasProperty(PropertyKey.ORE))
            secondaryByproductMultiplier = secondaryByproduct.getProperty(PropertyKey.ORE).getOreMultiplier();

        // Forge Hammer recipe
        // Washed Ore -> 2 Dust
        FORGE_HAMMER_RECIPES.recipeBuilder()
                .input(washed, material)
                .output(dust, material, 2 * property.getOreMultiplier())
                .duration(10).EUt(16).buildAndRegister();

        // Washed Ore -> Purified Ore
        if (material.hasProperty(PropertyKey.GEM)) {
            // Gems go in the Sifter
            SIFTER_RECIPES.recipeBuilder()
                    .input(washed, material)
                    .output(purified, material)
                    .chancedOutput(gemExquisite, material, property.getOreMultiplier(), 500, 0)
                    .chancedOutput(gemFlawless, material, property.getOreMultiplier(), 1000, 0)
                    .chancedOutput(gem, material, property.getOreMultiplier(), 2000, 0)
                    .chancedOutput(gemFlawed, material, property.getOreMultiplier(), 4000, 0)
                    .chancedOutput(gemChipped, material, property.getOreMultiplier(), 8000, 0)
                    .duration(200).EUt(64).buildAndRegister();
        } else if (material.hasFlag(PURIFY_BY_SIFTING)) {
            // Certain ores flagged to be in Sifter
            SIFTER_RECIPES.recipeBuilder()
                    .input(washed, material)
                    .output(purified, material)
                    .chancedOutput(primaryByproductPrefix, primaryByproduct, primaryByproductMultiplier, 4000, 0)
                    .chancedOutput(secondaryByproductPrefix, secondaryByproduct, secondaryByproductMultiplier, 6000, 0)
                    .duration(200).EUt(64).buildAndRegister();
        } else if (material.hasFlag(MAGNETIC_ORE) || primaryByproduct.hasFlag(MAGNETIC_ORE)) {
            // Magnetic Materials or Byproducts go in the Magnetic Separator
            ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder()
                    .input(washed, material)
                    .output(purified, material)
                    .chancedOutput(primaryByproductPrefix, primaryByproduct, primaryByproductMultiplier, 4000, 0)
                    .chancedOutput(secondaryByproductPrefix, secondaryByproduct, secondaryByproductMultiplier, 6000, 0)
                    .duration(200).EUt(64).buildAndRegister();
        }
        // Anything can go in the Centrifuge
        CENTRIFUGE_RECIPES.recipeBuilder()
                .input(washed, material)
                .output(purified, material)
                .chancedOutput(primaryByproductPrefix, primaryByproduct, primaryByproductMultiplier, 3000, 0)
                .chancedOutput(secondaryByproductPrefix, secondaryByproduct, secondaryByproductMultiplier, 4000, 0)
                .duration(200).EUt(64).buildAndRegister();
    }
}
