package gregtech.loaders.recipe.handlers.oreproc;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class PurifiedRecipeHandler {

    public static void processPurified(OrePrefix prefix, Material material, OreProperty property) {
        // Get the byproducts used for this step
        Material primaryByproduct = GTUtility.getOrDefault(property.getOreByProducts(), 1, material);
        OrePrefix primaryByproductPrefix = primaryByproduct.hasProperty(PropertyKey.GEM) ? gem : dust;
        int primaryByproductMultiplier = 1;
        if (primaryByproduct.hasProperty(PropertyKey.ORE))
            primaryByproductMultiplier = primaryByproduct.getProperty(PropertyKey.ORE).getOreMultiplier();

        Material tertiaryByproduct = GTUtility.getOrDefault(property.getOreByProducts(), 3, material);
        int tertiaryByproductMultiplier = 1;
        if (tertiaryByproduct.hasProperty(PropertyKey.ORE))
            tertiaryByproductMultiplier = tertiaryByproduct.getProperty(PropertyKey.ORE).getOreMultiplier();

        // Centrifuge recipe
        // Purified Ore -> 3 Dust
        CENTRIFUGE_RECIPES.recipeBuilder()
                .input(purified, material)
                .output(dust, material, property.getOreMultiplier() * 3)
                .duration(200).EUt(VA[ULV]).buildAndRegister();

        // Thermal Centrifuge recipes
        THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
                .input(purified, material)
                .output(refined, material)
                .output(dust, primaryByproduct, primaryByproductMultiplier)
                .chancedOutput(dust, tertiaryByproduct, tertiaryByproductMultiplier, 3000, 0)
                .duration(200).EUt(256).buildAndRegister();

        // Chemical Bath recipes
        if (property.getBathFluidOutputs() != null) {
            if (property.getBathItemOutput() != null) {
                CHEMICAL_BATH_RECIPES.recipeBuilder()
                        .input(purified, material)
                        .fluidInputs(property.getBathInputStack())
                        .output(refined, material)
                        .output(dust, primaryByproduct, primaryByproductMultiplier)
                        .outputs(property.getBathItemOutputStack())
                        .fluidOutputs(property.getBathFluidOutputStacks())
                        .duration(200).EUt(256).buildAndRegister();
            } else {
                CHEMICAL_BATH_RECIPES.recipeBuilder()
                        .input(purified, material)
                        .fluidInputs(property.getBathInputStack())
                        .output(refined, material)
                        .output(dust, primaryByproduct, primaryByproductMultiplier)
                        .fluidOutputs(property.getBathFluidOutputStacks())
                        .duration(200).EUt(256).buildAndRegister();
            }
        }
    }
}
