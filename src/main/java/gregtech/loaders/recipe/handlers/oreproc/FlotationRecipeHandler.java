package gregtech.loaders.recipe.handlers.oreproc;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import static gregtech.api.unification.material.Materials.*;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;

import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.api.unification.ore.OrePrefix.dustImpure;
import static gregtech.api.recipes.RecipeMaps.*;


public class FlotationRecipeHandler {
    public static void processFlotation(OrePrefix prefix, Material material, OreProperty property) {
        // Get the byproduct used for this step (Byp 4)
        Material byproduct = GTUtility.getOrDefault(property.getOreByProducts(), 3, material);
        OrePrefix byproductPrefix = byproduct.hasProperty(PropertyKey.GEM) ? gem : dust;
        int byproductMultiplier = 1;
        if (byproduct.hasProperty(PropertyKey.ORE))
            byproductMultiplier = byproduct.getProperty(PropertyKey.ORE).getOreMultiplier();

        // TODO flotation fluid
        // Flotation recipe
        // Purified Dust -> 8/7 Dust + 3/7 4th Byproduct (4 Dust per Ore)
        FLOTATION_RECIPES.recipeBuilder()
                .input(dustImpure, material)
                .output(dust, material, property.getOreMultiplier())
                .chancedOutput(dust, material, property.getOreMultiplier(), 1429, 0)
                .chancedOutput(dust, byproduct, byproductMultiplier, 4286, 0)
                .fluidOutputs(SluiceJuice.getFluid(1000))
                .buildAndRegister();
    }
}
