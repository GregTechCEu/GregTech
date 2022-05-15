package gregtech.loaders.recipe.handlers.oreproc;

import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;

import static gregtech.api.GTValues.LV;
import static gregtech.api.GTValues.VA;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

/**
 * Class for special Ore-related recipes.
 * Includes:
 * - Aqua Regia Bathing recipes
 * - Arc Furnace Roasting recipes
 * - Some special decomposition recipes for some Ore Dusts
 */
public class SpecialRecipeHandler {

    public static void init() {

        // Special Bathing Recipes

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedPurified, Cassiterite)
                .fluidInputs(SulfuricAcid.getFluid(500))
                .output(crushedRefined, Cassiterite)
                .chancedOutput(crushedRefined, Zinc, 5000, 0)
                .fluidOutputs(WhiteVitriol.getFluid(500))
                .fluidOutputs(Hydrogen.getFluid(1000))
                .duration(400).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedPurified, CassiteriteSand)
                .fluidInputs(SulfuricAcid.getFluid(500))
                .output(crushedRefined, CassiteriteSand)
                .chancedOutput(crushedRefined, Zinc, 5000, 0)
                .fluidOutputs(WhiteVitriol.getFluid(500))
                .fluidOutputs(Hydrogen.getFluid(1000))
                .duration(400).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedPurified, Tin)
                .fluidInputs(SulfuricAcid.getFluid(500))
                .output(crushedRefined, Tin)
                .chancedOutput(crushedRefined, Zinc, 5000, 0)
                .fluidOutputs(WhiteVitriol.getFluid(500))
                .fluidOutputs(Hydrogen.getFluid(1000))
                .duration(400).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedPurified, Copper)
                .fluidInputs(AquaRegia.getFluid(6000))
                .output(crushedRefined, Copper)
                .chancedOutput(crushedRefined, Cobalt, 5000, 0)
                .fluidOutputs(ChloroauricAcid.getFluid(500))
                .fluidOutputs(NitrogenDioxide.getFluid(2000))
                .fluidOutputs(Water.getFluid(2000))
                .duration(400).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedPurified, Gold)
                .fluidInputs(AquaRegia.getFluid(6000))
                .output(crushedRefined, Gold)
                .chancedOutput(crushedRefined, Copper, 5000, 0)
                .fluidOutputs(ChloroauricAcid.getFluid(500))
                .fluidOutputs(NitrogenDioxide.getFluid(2000))
                .fluidOutputs(Water.getFluid(2000))
                .duration(400).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedPurified, Cooperite)
                .fluidInputs(AquaRegia.getFluid(9000))
                .output(crushedRefined, Cooperite)
                .chancedOutput(dust, PlatinumGroupSludge, 2, 5000, 0)
                .fluidOutputs(ChloroplatinicAcid.getFluid(500))
                .fluidOutputs(NitrogenDioxide.getFluid(3000))
                .fluidOutputs(Water.getFluid(3000))
                .duration(400).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedPurified, Platinum)
                .fluidInputs(AquaRegia.getFluid(9000))
                .output(crushedRefined, Platinum)
                .chancedOutput(dust, PlatinumGroupSludge, 2, 5000, 0)
                .fluidOutputs(ChloroplatinicAcid.getFluid(500))
                .fluidOutputs(NitrogenDioxide.getFluid(3000))
                .fluidOutputs(Water.getFluid(3000))
                .duration(400).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedPurified, Palladium)
                .fluidInputs(AquaRegia.getFluid(9000))
                .output(crushedRefined, Palladium)
                .chancedOutput(dust, PlatinumGroupSludge, 2, 5000, 0)
                .fluidOutputs(ChloroplatinicAcid.getFluid(500))
                .fluidOutputs(NitrogenDioxide.getFluid(3000))
                .fluidOutputs(Water.getFluid(3000))
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
                .chancedOutput(chunk, Gallium, 2000, 1000)
                .fluidOutputs(SulfurDioxide.getFluid(1000))
                .duration(200).EUt(VA[LV]).buildAndRegister();

        // Special Decomposition Recipes

        // Granitic Mineral Sand
        SIFTER_RECIPES.recipeBuilder().EUt(30).duration(100)
                .input(dust, GraniticMineralSand, 9)
                .chancedOutput(dust, GraniteBlack, 3, 7500, 0)
                .chancedOutput(dust, Magnetite, 3, 5000, 0)
                .chancedOutput(nugget, Silver, 6, 2500, 0)
                .chancedOutput(nugget, Lead, 12, 2500, 0)
                .chancedOutput(nugget, Cobalt, 3, 2500, 0)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().EUt(30).duration(100)
                .input(dust, GraniticMineralSand, 9)
                .chancedOutput(dust, GraniteBlack, 6, 7500, 0)
                .chancedOutput(dust, Magnetite, 3, 5000, 0)
                .chancedOutput(dustTiny, Vanadium, 6, 2500, 0)
                .chancedOutput(nugget, Lead, 6, 2500, 0)
                .chancedOutput(nugget, Cobalt, 3, 2500, 0)
                .buildAndRegister();

        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder().EUt(30).duration(100)
                .input(dust, GraniticMineralSand, 9)
                .chancedOutput(dust, GraniteBlack, 3, 7500, 0)
                .chancedOutput(dust, Magnetite, 6, 5000, 0)
                .chancedOutput(nugget, Silver, 12, 2500, 0)
                .chancedOutput(nugget, Lead, 6, 2500, 0)
                .chancedOutput(nugget, Cobalt, 6, 2500, 0)
                .buildAndRegister();

        // Basaltic Mineral Sand
        SIFTER_RECIPES.recipeBuilder().EUt(30).duration(100)
                .input(dust, BasalticMineralSand, 9)
                .chancedOutput(dust, Basalt, 3, 7500, 0)
                .chancedOutput(dust, Magnetite, 3, 5000, 0)
                .chancedOutput(nugget, Gold, 6, 2500, 0)
                .chancedOutput(nugget, Copper, 12, 2500, 0)
                .chancedOutput(nugget, Nickel, 3, 2500, 0)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().EUt(30).duration(100)
                .input(dust, BasalticMineralSand, 9)
                .chancedOutput(dust, Basalt, 6, 7500, 0)
                .chancedOutput(dust, Magnetite, 3, 5000, 0)
                .chancedOutput(dustTiny, Vanadium, 6, 2500, 0)
                .chancedOutput(nugget, Copper, 6, 2500, 0)
                .chancedOutput(nugget, Nickel, 3, 2500, 0)
                .buildAndRegister();

        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder().EUt(30).duration(100)
                .input(dust, BasalticMineralSand, 9)
                .chancedOutput(dust, Basalt, 3, 7500, 0)
                .chancedOutput(dust, Magnetite, 6, 5000, 0)
                .chancedOutput(nugget, Gold, 12, 2500, 0)
                .chancedOutput(nugget, Copper, 6, 2500, 0)
                .chancedOutput(nugget, Nickel, 6, 2500, 0)
                .buildAndRegister();

        // Glauconite Sand
        SIFTER_RECIPES.recipeBuilder().EUt(30).duration(100)
                .input(dust, GlauconiteSand, 9)
                .chancedOutput(dust, Mica, 3, 7500, 0)
                .chancedOutput(dust, Gypsum, 6, 5000, 0)
                .chancedOutput(dustTiny, Monazite, 6, 2500, 0)
                .chancedOutput(dustTiny, Barium, 12, 2500, 0)
                .chancedOutput(dustTiny, Rutile, 3, 2500, 0)
                .buildAndRegister();

        // Garnet Sand
        SIFTER_RECIPES.recipeBuilder().EUt(30).duration(100)
                .input(dust, GarnetSand)
                .chancedOutput(gem, Almandine, 1, 2000, 0)
                .chancedOutput(gem, Andradite, 1, 2000, 0)
                .chancedOutput(gem, Grossular, 1, 2000, 0)
                .chancedOutput(gem, Pyrope, 1, 2000, 0)
                .chancedOutput(gem, Spessartine, 1, 2000, 0)
                .chancedOutput(gem, Uvarovite, 1, 2000, 0)
                .buildAndRegister();
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
