package gregtech.loaders.recipe.handlers.oreproc;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;

import static gregtech.api.recipes.RecipeMaps.SLUICE_RECIPES;
import static gregtech.api.unification.material.Materials.SluiceJuice;
import static gregtech.api.unification.material.Materials.Water;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.loaders.recipe.handlers.oreproc.OreRecipeHandler.processMetalSmelting;

public class DustRecipeHandler {

    public static void processImpure(OrePrefix prefix, Material material, OreProperty property) {
        // Get the byproduct to use for this step
        Material byproduct = GTUtility.getOrDefault(property.getOreByProducts(), 1, material);
        OrePrefix byproductPrefix = byproduct.hasProperty(PropertyKey.GEM) ? gem : dust;
        int byproductMultiplier = 1;
        if (byproduct.hasProperty(PropertyKey.ORE))
            byproductMultiplier = byproduct.getProperty(PropertyKey.ORE).getOreMultiplier();

        // Sluice recipe
        // Impure Dust -> Dust (no byproduct)
        SLUICE_RECIPES.recipeBuilder()
                .input(dustImpure, material, property.getOreMultiplier())
                .fluidInputs(Water.getFluid(100))
                .output(dust, material, property.getOreMultiplier())
                .chancedOutput(byproductPrefix, byproduct, byproductMultiplier, 500, 0)
                .fluidOutputs(SluiceJuice.getFluid(100))
                .duration(8).EUt(4).buildAndRegister();

        // Smelting recipe
        processMetalSmelting(prefix, material, property);
    }

    public static void processDust(OrePrefix prefix, Material material, OreProperty property) {
        // Smelting recipe
        processMetalSmelting(prefix, material, property);
    }
}
