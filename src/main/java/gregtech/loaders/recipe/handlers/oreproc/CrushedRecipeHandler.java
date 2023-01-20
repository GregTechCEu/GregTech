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
import static gregtech.api.unification.material.info.MaterialFlags.PURIFY_BY_SIFTING;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.loaders.recipe.handlers.oreproc.OreRecipeHandler.processMetalSmelting;

public class CrushedRecipeHandler {

    public static void processCrushed(OrePrefix prefix, Material material, OreProperty property) {
        // Get the byproduct used for this step
        Material byproduct = GTUtility.getOrDefault(property.getOreByProducts(), 1, material);
        OrePrefix byproductPrefix = byproduct.hasProperty(PropertyKey.GEM) ? gem : dust;
        int byproductMultiplier = 1;
        if (byproduct.hasProperty(PropertyKey.ORE))
            byproductMultiplier = byproduct.getProperty(PropertyKey.ORE).getOreMultiplier();


        // Forge Hammer recipe
        // Crushed Ore -> Impure Dust
        FORGE_HAMMER_RECIPES.recipeBuilder()
                .input(crushed, material)
                .output(dustImpure, material, property.getOreMultiplier())
                .duration(10).EUt(16).buildAndRegister();

        // Macerator recipe
        // Crushed Ore -> Dust (without byproduct)
        MACERATOR_RECIPES.recipeBuilder()
                .input(crushed, material)
                .output(dust, material, property.getOreMultiplier())
                .chancedOutput(dust, Stone, 7500, 0)
                .duration(256).EUt(2).buildAndRegister();


        // Sluice recipes
        // Crushed Ore -> Purified Ore + Byproduct Dust (Water)
        SLUICE_RECIPES.recipeBuilder()
                .input(crushed, material)
                .fluidInputs(Water.getFluid(1000))
                .output(crushedPurified, material)
                .chancedOutput(byproductPrefix, byproduct, byproductMultiplier, 3750, 0)
                .output(dust, Stone)
                .fluidOutputs(SluiceJuice.getFluid(1000))
                .duration(256).EUt(16).buildAndRegister();

        // Crushed Ore -> Purified Ore + Byproduct Purified Ore (Persulfate)
        if (byproduct.hasProperty(PropertyKey.ORE)) {
            SLUICE_RECIPES.recipeBuilder()
                    .input(crushed, material)
                    .fluidInputs(SodiumPersulfate.getFluid(125))
                    .output(crushedPurified, material)
                    .chancedOutput(crushedPurified, byproduct, byproductMultiplier, 3750, 0)
                    .output(dust, Stone)
                    .fluidOutputs(SluiceJuice.getFluid(500))
                    .duration(256).EUt(64).buildAndRegister();

            SLUICE_RECIPES.recipeBuilder()
                    .input(crushed, material)
                    .fluidInputs(PotassiumPersulfate.getFluid(125))
                    .output(crushedPurified, material)
                    .chancedOutput(crushedPurified, byproduct, byproductMultiplier, 3750, 0)
                    .output(dust, Stone)
                    .fluidOutputs(SluiceJuice.getFluid(500))
                    .duration(256).EUt(64).buildAndRegister();
        }

        // Sifter recipe (for Gems)
        // TODO remove flawless emerald from mv emitter so that this awkward step isn't needed?
        if (material.hasProperty(PropertyKey.GEM)) {
            SIFTER_RECIPES.recipeBuilder()
                    .input(crushed, material)
                    .output(crushedRefined, material)
                    .chancedOutput(gemFlawless, material, property.getOreMultiplier(), 500, 0)
                    .chancedOutput(gem, material, property.getOreMultiplier(), 1000, 0)
                    .chancedOutput(byproductPrefix, byproduct, byproductMultiplier, 2000, 0)
                    .duration(256).EUt(64).buildAndRegister();
        }

        // Hard Hammer crafting recipe
        // Crushed Ore -> Impure Dust
        ModHandler.addShapelessRecipe(String.format("crushed_ore_to_dust_%s", material),
                OreDictUnifier.get(dustImpure, material, property.getOreMultiplier()), 'h', new UnificationEntry(crushed, material));

        processMetalSmelting(prefix, material, property);
    }
}
