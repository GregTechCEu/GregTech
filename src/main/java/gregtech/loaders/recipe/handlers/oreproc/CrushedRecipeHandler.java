package gregtech.loaders.recipe.handlers.oreproc;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;

import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class CrushedRecipeHandler {

    public static void processCrushed(OrePrefix prefix, Material material, OreProperty property) {
        // Get the byproduct used for this step
        Material byproduct = GTUtility.getOrDefault(property.getOreByProducts(), 1, material);
        OrePrefix byproductPrefix = byproduct.hasProperty(PropertyKey.GEM) ? gem : dust;
        int byproductMultiplier = 1;
        if (byproduct.hasProperty(PropertyKey.ORE))
            byproductMultiplier = byproduct.getProperty(PropertyKey.ORE).getOreMultiplier();


        // Forge Hammer recipe
        // 2 Crushed Ore -> 3 Dust
        if (property.getOreMultiplier() % 2 == 0){
            FORGE_HAMMER_RECIPES.recipeBuilder()
                    .input(crushed, material)
                    .output(dust, material, property.getOreMultiplier() * 3 / 2)
                    .duration(10).EUt(16).buildAndRegister();
        } else {
            FORGE_HAMMER_RECIPES.recipeBuilder()
                    .input(crushed, material, 2)
                    .output(dust, material, 3 * property.getOreMultiplier())
                    .duration(20).EUt(16).buildAndRegister();
        }

        // Sluice recipes
        // Crushed Ore -> Purified Ore + Byproduct Dust (Water)
        SLUICE_RECIPES.recipeBuilder()
                .input(crushed, material)
                .fluidInputs(Water.getFluid(1000))
                .output(washed, material)
                .chancedOutput(byproductPrefix, byproduct, byproductMultiplier, 7500, 0)
                .fluidOutputs(SluiceJuice.getFluid(1000))
                .duration(200).EUt(16).buildAndRegister();

        // Crushed Ore -> Purified Ore + Byproduct Purified Ore (Persulfate)
        if (byproduct.hasProperty(PropertyKey.ORE)) {
            SLUICE_RECIPES.recipeBuilder()
                    .input(crushed, material)
                    .fluidInputs(SodiumPersulfate.getFluid(250))
                    .output(washed, material)
                    .chancedOutput(washed, byproduct, byproductMultiplier, 7500, 0)
                    .output(dust, SluiceSand)
                    .duration(200).EUt(64).buildAndRegister();
        }
    }
}
