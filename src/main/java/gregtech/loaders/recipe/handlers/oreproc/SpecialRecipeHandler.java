package gregtech.loaders.recipe.handlers.oreproc;

import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;

import static gregtech.api.GTValues.LV;
import static gregtech.api.GTValues.VA;
import static gregtech.api.recipes.RecipeMaps.ARC_FURNACE_RECIPES;
import static gregtech.api.recipes.RecipeMaps.CHEMICAL_BATH_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class SpecialRecipeHandler {

    public static void init() {

        // Special Bathing Recipes

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedPurified, Cassiterite)
                .fluidInputs(AquaRegia.getFluid(1000))
                .output(crushedRefined, Cassiterite)
                .chancedOutput(crushedRefined, Zinc, 5000, 0)
                .fluidOutputs(StannicChloride.getFluid(500))
                .fluidOutputs(NitricOxide.getFluid(1000))
                .fluidOutputs(Water.getFluid(2000))
                .duration(400).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedPurified, CassiteriteSand)
                .fluidInputs(AquaRegia.getFluid(1000))
                .output(crushedRefined, CassiteriteSand)
                .chancedOutput(crushedRefined, Zinc, 5000, 0)
                .fluidOutputs(StannicChloride.getFluid(500))
                .fluidOutputs(NitricOxide.getFluid(1000))
                .fluidOutputs(Water.getFluid(2000))
                .duration(400).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedPurified, Tin)
                .fluidInputs(AquaRegia.getFluid(1000))
                .output(crushedRefined, Tin)
                .chancedOutput(crushedRefined, Zinc, 5000, 0)
                .fluidOutputs(StannicChloride.getFluid(500))
                .fluidOutputs(NitricOxide.getFluid(1000))
                .fluidOutputs(Water.getFluid(2000))
                .duration(400).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedPurified, Copper)
                .fluidInputs(AquaRegia.getFluid(1000))
                .output(crushedRefined, Copper)
                .chancedOutput(crushedRefined, Cobalt, 5000, 0)
                .fluidOutputs(ChloroauricAcid.getFluid(500))
                .fluidOutputs(NitricOxide.getFluid(1000))
                .fluidOutputs(Water.getFluid(2000))
                .duration(400).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedPurified, Gold)
                .fluidInputs(AquaRegia.getFluid(1000))
                .output(crushedRefined, Gold)
                .chancedOutput(crushedRefined, Copper, 5000, 0)
                .fluidOutputs(ChloroauricAcid.getFluid(500))
                .fluidOutputs(NitricOxide.getFluid(1000))
                .fluidOutputs(Water.getFluid(2000))
                .duration(400).EUt(VA[LV]).buildAndRegister();

        // Roasting Recipes
        addRoastingRecipe(Chalcopyrite);
        addRoastingRecipe(Cobaltite);
        addRoastingRecipe(Cooperite);
        addRoastingRecipe(Galena);
        addRoastingRecipe(Molybdenite);
        addRoastingRecipe(Pyrite);
        addRoastingRecipe(Stibnite);
        addRoastingRecipe(Tetrahedrite);
        addRoastingRecipe(Chalcocite);
        addRoastingRecipe(Bornite);

        // Pentlandite done separately because of its formula
        ARC_FURNACE_RECIPES.recipeBuilder()
                .input(dust, Pentlandite, 2)
                .fluidInputs(Oxygen.getFluid(2000))
                .output(ingot, Nickel)
                .fluidOutputs(SulfurDioxide.getFluid(1000))
                .duration(200).EUt(VA[LV]).buildAndRegister();

        // Realgar done separately because of its formula
        ARC_FURNACE_RECIPES.recipeBuilder()
                .input(dust, Realgar, 2)
                .fluidInputs(Oxygen.getFluid(2000))
                .output(dust, Arsenic)
                .fluidOutputs(SulfurDioxide.getFluid(1000))
                .duration(200).EUt(VA[LV]).buildAndRegister();

        // Sphalerite done separately because of the Gallium output
        ARC_FURNACE_RECIPES.recipeBuilder()
                .input(dust, Sphalerite, 2)
                .fluidInputs(Oxygen.getFluid(2000))
                .output(ingot, Zinc)
                .chancedOutput(dustSmall, Gallium, 2000, 1000)
                .fluidOutputs(SulfurDioxide.getFluid(1000))
                .duration(200).EUt(VA[LV]).buildAndRegister();
    }

    /**
     * Creates a Roaster Recipe for a Material.
     * The recipe will follow the pattern:
     * - 1 mol of Dust input
     * - 2kL of Oxygen for each Sulfur present in the Material
     * - Output the Sulfur as SO2
     * - Output the other parts as Ingots if possible (Dusts if Ingot is unavailable or requires EBF)
     * - 30 EU/t, 10 seconds
     *
     * Designed for ONLY FIRST DEGREE MATERIALS!!
     */
    private static void addRoastingRecipe(Material input) {
        int materialAmount = input.getNumComponents();

        RecipeBuilder<?> builder = ARC_FURNACE_RECIPES.recipeBuilder()
                .input(dust, input, materialAmount)
                .duration(200).EUt(VA[LV]);

        for (MaterialStack ms : input.getMaterialComponents()) {
            if (ms.material == Sulfur) {
                builder.fluidInputs(Oxygen.getFluid((int) (2000 * ms.amount)))
                        .fluidOutputs(SulfurDioxide.getFluid((int) (1000 * ms.amount)));
            } else {
                OrePrefix prefix = dust;
                if (ms.material.hasProperty(PropertyKey.INGOT) && !ms.material.hasProperty(PropertyKey.BLAST)) {
                    prefix = ingot;
                }
                builder.output(prefix, ms.material, (int) ms.amount);
            }
        }
        builder.buildAndRegister();
    }
}
