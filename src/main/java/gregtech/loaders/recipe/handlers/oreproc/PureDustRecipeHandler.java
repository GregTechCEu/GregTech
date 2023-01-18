package gregtech.loaders.recipe.handlers.oreproc;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;

import static gregtech.api.GTValues.ULV;
import static gregtech.api.GTValues.VA;
import static gregtech.api.unification.material.Materials.*;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;

import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.api.recipes.RecipeMaps.*;


public class PureDustRecipeHandler {
    public static void processFlotation(OrePrefix prefix, Material material, OreProperty property) {
        // Get the byproduct used for this step (Byp 3)
        Material byproduct = GTUtility.getOrDefault(property.getOreByProducts(), 2, material);
        OrePrefix byproductPrefix = byproduct.hasProperty(PropertyKey.GEM) ? gem : dust;
        int byproductMultiplier = 1;
        if (byproduct.hasProperty(PropertyKey.ORE))
            byproductMultiplier = byproduct.getProperty(PropertyKey.ORE).getOreMultiplier();

        // Centrifuge
        // Purified Dust -> 1.5 Dust
        CENTRIFUGE_RECIPES.recipeBuilder()
                .input(dustPure, material)
                .output(dust, material, property.getOreMultiplier())
                .chancedOutput(dust, material, property.getOreMultiplier(), 5000, 0)
                .duration(400).EUt(VA[ULV]).buildAndRegister();

        // Flotation recipe
        if (byproduct.hasProperty(PropertyKey.ORE)) {
            // Purified Dust -> 2 Dust + 0.15 3rd Byproduct Purified Dust (4 Dust per Ore)
            FLOTATION_RECIPES.recipeBuilder()
                    .input(dustPure, material)
                    .fluidInputs(SodiumEthylXanthate.getFluid(1000))
                    .output(dust, material, 2 * property.getOreMultiplier())
                    .chancedOutput(dustPure, byproduct, byproductMultiplier, 1500, 0)
                    .fluidOutputs(SluiceJuice.getFluid(1500))
                    .buildAndRegister();
        } else {
            // Purified Dust -> 2 Dust + 0.5 3rd Byproduct (4 Dust per Ore)
            FLOTATION_RECIPES.recipeBuilder()
                    .input(dustPure, material)
                    .fluidInputs(SodiumEthylXanthate.getFluid(1000))
                    .output(dust, material, 2 * property.getOreMultiplier())
                    .chancedOutput(dust, byproduct, byproductMultiplier, 5000, 0)
                    .fluidOutputs(SluiceJuice.getFluid(1500))
                    .buildAndRegister();
        }
    }
}
