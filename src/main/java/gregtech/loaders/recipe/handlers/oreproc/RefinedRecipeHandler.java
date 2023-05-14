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


public class RefinedRecipeHandler {
    public static void processRefined(OrePrefix prefix, Material material, OreProperty property) {
        // Get the byproduct used for this step (Byp 3)
        Material byproduct = GTUtility.getOrDefault(property.getOreByProducts(), 3, material);

        // Centrifuge
        // Refined Ore -> 4 Dust
        CENTRIFUGE_RECIPES.recipeBuilder()
                .input(refined, material)
                .output(dust, material, 4 * property.getOreMultiplier())
                .duration(200).EUt(VA[ULV]).buildAndRegister();

        // Flotation recipe
        if (byproduct.hasProperty(PropertyKey.ORE)) {
            // Refined Ore -> 6 Dust + 0.3 3rd Byproduct Refined Ore
            FLOTATION_RECIPES.recipeBuilder()
                    .input(refined, material)
                    .fluidInputs(SodiumEthylXanthate.getFluid(250))
                    .output(dust, material, 6 * property.getOreMultiplier())
                    .chancedOutput(refined, byproduct, 3000, 0)
                    .fluidOutputs(FlotationWaste.getFluid(100))
                    .buildAndRegister();
        } else {
            // Refined Ore -> 6 Dust + 1 3rd Byproduct
            FLOTATION_RECIPES.recipeBuilder()
                    .input(refined, material)
                    .fluidInputs(SodiumEthylXanthate.getFluid(250))
                    .output(dust, material, 6 * property.getOreMultiplier())
                    .output(dust, byproduct)
                    .fluidOutputs(FlotationWaste.getFluid(100))
                    .buildAndRegister();
        }
    }
}
