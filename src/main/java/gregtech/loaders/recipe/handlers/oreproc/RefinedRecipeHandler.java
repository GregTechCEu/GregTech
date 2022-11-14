package gregtech.loaders.recipe.handlers.oreproc;

import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;

import static gregtech.api.GTValues.LV;
import static gregtech.api.GTValues.VA;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.info.MaterialFlags.WASHING_MERCURY;
import static gregtech.api.unification.material.info.MaterialFlags.WASHING_PERSULFATE;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.loaders.recipe.handlers.oreproc.OreRecipeHandler.processMetalSmelting;

public class RefinedRecipeHandler {

    public static void processRefined(OrePrefix prefix, Material material, OreProperty property) {
        boolean chancePerTier = ConfigHolder.recipes.oreByproductChancePerTier;
        // Get the byproduct used for this step
        Material byproduct = GTUtility.getOrDefault(property.getOreByProducts(), 0, material);
        OrePrefix byproductPrefix = byproduct.hasProperty(PropertyKey.GEM) ? gem : dust;
        int byproductMultiplier = 1;
        if (byproduct.hasProperty(PropertyKey.ORE))
            byproductMultiplier = byproduct.getProperty(PropertyKey.ORE).getOreMultiplier();

        // Forge Hammer recipe
        // Centrifuged Ore -> Dust
        FORGE_HAMMER_RECIPES.recipeBuilder()
                .input(crushedRefined, material)
                .output(dust, material, property.getOreMultiplier())
                .duration(10).EUt(16).buildAndRegister();

        // Macerator recipe
        // Centrifuged Ore -> Dust
        MACERATOR_RECIPES.recipeBuilder()
                .input(crushedRefined, material)
                .output(dust, material, property.getOreMultiplier())
                .chancedOutput(dust, material, property.getOreMultiplier(), 3333, 0)
                .chancedOutput(byproductPrefix, byproduct, byproductMultiplier, 2500, chancePerTier ? 500 : 0)
                .duration(400).EUt(2).buildAndRegister();

        // Hard Hammer crafting recipe
        // Centrifuged Ore -> Dust
        ModHandler.addShapelessRecipe(String.format("centrifuged_ore_to_dust_%s", material),
                OreDictUnifier.get(dust, material, property.getOreMultiplier()), 'h', new UnificationEntry(crushedRefined, material));

        processMetalSmelting(prefix, material, property);

        // Chemical Bath recipe (if applicable)
        // Refined Ore -> 2x Dust + Vitriol
        OreProperty byproductProp = byproduct.getProperty(PropertyKey.ORE);
        if (byproductProp != null && byproductProp.getBathRecipe() != null) {
            byproductProp.getBathRecipe().accept(material, property, byproduct);
        } else {
            // Chemical Bath recipe
            // Refined Ore -> Dust + byproduct
            // Only applies if a byproduct in this Material's byproduct
            // list contains either the WASHING_MERCURY or
            // WASHING_PERSULFATE flags

            if (byproduct.hasFlag(WASHING_MERCURY)) {
                CHEMICAL_BATH_RECIPES.recipeBuilder()
                        .input(crushedRefined, material)
                        .fluidInputs(Materials.Mercury.getFluid(100))
                        .output(dust, material, 2 * property.getOreMultiplier())
                        .chancedOutput(dust, byproduct, byproductMultiplier, 5000, 0)
                        .duration(400).EUt(VA[LV]).buildAndRegister();
            }

            if (byproduct.hasFlag(WASHING_PERSULFATE)) {
                CHEMICAL_BATH_RECIPES.recipeBuilder()
                        .input(crushedRefined, material)
                        .fluidInputs(Materials.SodiumPersulfate.getFluid(100))
                        .output(dust, material, 2 * property.getOreMultiplier())
                        .chancedOutput(dust, byproduct, byproductMultiplier, 5000, 0)
                        .duration(400).EUt(VA[LV]).buildAndRegister();

                CHEMICAL_BATH_RECIPES.recipeBuilder()
                        .input(crushedRefined, material)
                        .fluidInputs(Materials.PotassiumPersulfate.getFluid(100))
                        .output(dust, material, 2 * property.getOreMultiplier())
                        .chancedOutput(dust, byproduct, byproductMultiplier, 5000, 0)
                        .duration(400).EUt(VA[LV]).buildAndRegister();
            }
        }
    }
}
