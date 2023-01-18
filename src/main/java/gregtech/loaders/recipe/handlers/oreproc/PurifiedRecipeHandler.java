package gregtech.loaders.recipe.handlers.oreproc;

import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;

import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.MAGNETIC_ORE;
import static gregtech.api.unification.material.info.MaterialFlags.REFINE_BY_SIFTING;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.loaders.recipe.handlers.oreproc.OreRecipeHandler.processMetalSmelting;

public class PurifiedRecipeHandler {

    public static void processPurified(OrePrefix prefix, Material material, OreProperty property) {
        // Get the byproduct used for this step
        Material byproduct = GTUtility.getOrDefault(property.getOreByProducts(), 1, material);
        OrePrefix byproductPrefix = byproduct.hasProperty(PropertyKey.GEM) ? gem : dust;
        int byproductMultiplier = 1;
        if (byproduct.hasProperty(PropertyKey.ORE))
            byproductMultiplier = byproduct.getProperty(PropertyKey.ORE).getOreMultiplier();

        // Forge Hammer recipe
        // Purified Ore -> Dust
        FORGE_HAMMER_RECIPES.recipeBuilder()
                .input(crushedPurified, material)
                .output(dustImpure, material, property.getOreMultiplier())
                .duration(10).EUt(16).buildAndRegister();

        // Macerator recipe
        // Purified Ore -> Dust
        MACERATOR_RECIPES.recipeBuilder()
                .input(crushedPurified, material)
                .output(dust, material, property.getOreMultiplier())
                .chancedOutput(dust, material, property.getOreMultiplier(), 1666, 0)
                .duration(256).EUt(2).buildAndRegister();

        // Sluice recipes
        // Crushed Ore -> Purified Ore + Byproduct Dust (Water)
        SLUICE_RECIPES.recipeBuilder()
                .input(crushedPurified, material)
                .fluidInputs(Water.getFluid(1000))
                .output(crushedRefined, material)
                .chancedOutput(byproductPrefix, byproduct, byproductMultiplier, 3750, 0)
                .output(dust, Stone)
                .fluidOutputs(SluiceJuice.getFluid(1000))
                .duration(256).EUt(16).buildAndRegister();

        // Crushed Ore -> Purified Ore + Byproduct Purified Ore (Persulfate)
        if (byproduct.hasProperty(PropertyKey.ORE)) {
            SLUICE_RECIPES.recipeBuilder()
                    .input(crushedPurified, material)
                    .fluidInputs(Water.getFluid(1000))
                    .output(crushedRefined, material)
                    .chancedOutput(crushedRefined, byproduct, byproductMultiplier, 3750, 0)
                    .output(dust, Stone)
                    .fluidOutputs(SluiceJuice.getFluid(500))
                    .duration(256).EUt(64).buildAndRegister();
        }


        // Sifter recipe (if applicable)
        // Purified Ore -> Gems of various sizes
        // TODO yeet
//        if (material.hasProperty(PropertyKey.GEM)) {
//            if (material.hasFlag(HIGH_SIFTER_OUTPUT)) {
//                SIFTER_RECIPES.recipeBuilder()
//                        .input(crushedPurified, material)
//                        .chancedOutput(gemExquisite, material, 500, 150)
//                        .chancedOutput(gemFlawless, material, 1500, 200)
//                        .chancedOutput(gem, material, 5000, 1000)
//                        .chancedOutput(dust, material, 2500, 500)
//                        .chancedOutput(gemFlawed, material, 2000, 500)
//                        .chancedOutput(gemChipped, material, 3000, 350)
//                        .duration(400).EUt(30).buildAndRegister();
//            } else {
//                SIFTER_RECIPES.recipeBuilder()
//                        .input(crushedPurified, material)
//                        .chancedOutput(gemExquisite, material, 300, 100)
//                        .chancedOutput(gemFlawless, material, 1000, 150)
//                        .chancedOutput(gem, material, 3500, 500)
//                        .chancedOutput(dust, material, 5000, 750)
//                        .chancedOutput(gemFlawed, material, 2500, 300)
//                        .chancedOutput(gemChipped, material, 3500, 400)
//                        .duration(400).EUt(30).buildAndRegister();
//            }
//        }

        // Chemical Bath recipe (if applicable)
        // Purified Ore -> Refined Ore (+ chance to double, + Vitriol)
        // TODO yeet (moved to refined -> dust)
//        if (property.getVitriol() != null) {
//            if (property.getVitriol() == ClayVitriol) { // handle this one differently since it is a different composition
//                CHEMICAL_BATH_RECIPES.recipeBuilder()
//                        .input(crushedPurified, material)
//                        .fluidInputs(SulfuricAcid.getFluid(1500))
//                        .output(crushedRefined, material)
//                        .chancedOutput(crushedRefined, material, 5000, 0)
//                        .fluidOutputs(property.getVitriol().getFluid(500))
//                        .fluidOutputs(Hydrogen.getFluid(3000))
//                        .duration(400).EUt(VA[LV]).buildAndRegister();
//            } else {
//                CHEMICAL_BATH_RECIPES.recipeBuilder()
//                        .input(crushedPurified, material)
//                        .fluidInputs(SulfuricAcid.getFluid(500))
//                        .output(crushedRefined, material)
//                        .chancedOutput(crushedRefined, material, 5000, 0)
//                        .fluidOutputs(property.getVitriol().getFluid(500))
//                        .fluidOutputs(Hydrogen.getFluid(1000))
//                        .duration(400).EUt(VA[LV]).buildAndRegister();
//            }
//        }

        // Hard Hammer crafting recipe
        // Purified Ore -> Dust
        ModHandler.addShapelessRecipe(String.format("purified_ore_to_dust_%s", material),
                OreDictUnifier.get(dustImpure, material, property.getOreMultiplier()), 'h', new UnificationEntry(crushedPurified, material));

        processMetalSmelting(prefix, material, property);
    }
}
