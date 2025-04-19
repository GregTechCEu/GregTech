package gtqt.loaders.recipe.handlers;

import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.stack.UnificationEntry;

import static gregtech.api.GTValues.*;
import static gregtech.api.GTValues.SECOND;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.Materials.NaquadahAlloy;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.api.unification.ore.OrePrefix.ring;
import static gtqt.common.items.GTQTMetaItems.*;

public class FakeToolRecipes {

    public static void register() {
        // Wooden Bucket
        ModHandler.addShapelessNBTClearingRecipe("wooden_bucket_nbt",
                WOODEN_BUCKET.getStackForm(),
                WOODEN_BUCKET.getStackForm());

        ModHandler.addShapedRecipe(true, "wooden_bucket", WOODEN_BUCKET.getStackForm(),
                "   ", "PhP", "RPR",
                'P', new UnificationEntry(plate, Wood),
                'R', new UnificationEntry(ring, Copper));

        // Chrome Fluid Cell
        ModHandler.addShapelessNBTClearingRecipe("cell_nbt_chrome",
                FLUID_CELL_CHROME.getStackForm(),
                FLUID_CELL_CHROME.getStackForm());

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plateDouble, Chrome, 6)
                .input(ring, Palladium, 6)
                .output(FLUID_CELL_CHROME)
                .EUt(VH[EV])
                .duration(10 * SECOND)
                .buildAndRegister();

        // Iridium Fluid Cell
        ModHandler.addShapelessNBTClearingRecipe("cell_nbt_iridium",
                FLUID_CELL_IRIDIUM.getStackForm(),
                FLUID_CELL_IRIDIUM.getStackForm());

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plateDouble, Iridium, 8)
                .input(ring, Naquadah, 8)
                .output(FLUID_CELL_IRIDIUM)
                .EUt(VA[EV])
                .duration(10 * SECOND)
                .buildAndRegister();

        // Naquadah Alloy Fluid Cell
        ModHandler.addShapelessNBTClearingRecipe("cell_nbt_naquadah_alloy",
                FLUID_CELL_NAQUADAH_ALLOY.getStackForm(),
                FLUID_CELL_NAQUADAH_ALLOY.getStackForm());

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plateDouble, NaquadahAlloy, 12)
                .input(ring, Tritanium, 12)
                .output(FLUID_CELL_NAQUADAH_ALLOY)
                .EUt(VH[IV])
                .duration(10 * SECOND)
                .buildAndRegister();

        // Neutronium Fluid Cell
        ModHandler.addShapelessNBTClearingRecipe("cell_nbt_neutronium",
                FLUID_CELL_NEUTRONIUM.getStackForm(),
                FLUID_CELL_NEUTRONIUM.getStackForm());

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plateDouble, Neutronium, 16)
                .input(ring, NaquadahAlloy, 16)
                .output(FLUID_CELL_NEUTRONIUM)
                .EUt(VA[IV])
                .duration(10 * SECOND)
                .buildAndRegister();

    }
}
