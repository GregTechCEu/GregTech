package gregtech.loaders.recipe.handlers.oreproc;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;

import static gregtech.api.recipes.RecipeMaps.CHEMICAL_BATH_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.loaders.recipe.handlers.oreproc.OreRecipeHandler.processMetalSmelting;

public class DustRecipeHandler {

    public static void processImpure(OrePrefix prefix, Material material, OreProperty property) {
        // Get the byproduct used for this step
        Material byproduct = GTUtility.selectItemInList(0, material, property.getOreByProducts(), Material.class);

        // Chemical Bath recipe
        // Impure Dust -> Dust (no byproduct)
        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(dustImpure, material)
                .fluidInputs(Water.getFluid(100))
                .output(dust, material)
                .output(dustTiny, Stone)
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
