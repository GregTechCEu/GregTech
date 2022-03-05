package gregtech.loaders.recipe.handlers.oreproc;

import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;

import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.Water;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.loaders.recipe.handlers.oreproc.OreRecipeHandler.processMetalSmelting;

public class DustRecipeHandler {

    public static void processImpure(OrePrefix prefix, Material material, OreProperty property) {
        // Get the byproduct used for this step
        Material byproduct = GTUtility.selectItemInList(0, material, property.getOreByProducts(), Material.class);

        // Centrifuge recipe
        // Impure Dust -> Dust
        CENTRIFUGE_RECIPES.recipeBuilder()
                .input(dustImpure, material)
                .output(dust, material)
                .output(dustTiny, byproduct)
                .duration(400).EUt(24)
                .buildAndRegister();

        // TODO Remove?
        // Ore Washer recipe
        // Impure Dust -> Dust (no byproduct)
        ORE_WASHER_RECIPES.recipeBuilder()
                .input(dustImpure, material)
                .notConsumable(new IntCircuitIngredient(2))
                .fluidInputs(Water.getFluid(100))
                .output(dust, material)
                .duration(8).EUt(4).buildAndRegister();

        // Smelting recipe
        processMetalSmelting(prefix, material, property);
    }

    public static void processDust(OrePrefix prefix, Material material, OreProperty property) {
        // Smelting recipe
        processMetalSmelting(prefix, material, property);
    }
}
