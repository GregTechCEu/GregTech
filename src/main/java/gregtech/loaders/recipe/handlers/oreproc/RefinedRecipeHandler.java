package gregtech.loaders.recipe.handlers.oreproc;

import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;

import static gregtech.api.GTValues.M;
import static gregtech.api.recipes.RecipeMaps.FORGE_HAMMER_RECIPES;
import static gregtech.api.recipes.RecipeMaps.MACERATOR_RECIPES;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.loaders.recipe.handlers.oreproc.OreRecipeHandler.processMetalSmelting;

public class RefinedRecipeHandler {

    public static void processRefined(OrePrefix prefix, Material material, OreProperty property) {
        boolean chancePerTier = ConfigHolder.recipes.oreByproductChancePerTier;
        // Get the byproduct used for this step
        Material byproduct = GTUtility.selectItemInList(2, material, property.getOreByProducts(), Material.class);
        OreProperty byproductProp = byproduct.getProperty(PropertyKey.ORE);

        int crushedMultiplier = (int) (crushed.getMaterialAmount(material) / M);

        // Forge Hammer recipe
        // Centrifuged Ore -> Dust
        FORGE_HAMMER_RECIPES.recipeBuilder()
                .input(crushedRefined, material)
                .output(dust, material, crushedMultiplier)
                .duration(10).EUt(16).buildAndRegister();

        // Macerator recipe
        // Centrifuged Ore -> Dust
        MACERATOR_RECIPES.recipeBuilder()
                .input(crushedRefined, material)
                .output(dust, material, crushedMultiplier)
                .chancedOutput(dust, material, crushedMultiplier, 3333, 0)
                .chancedOutput(dust, byproduct, 2500, chancePerTier ? 500 : 0)
                .duration(400).EUt(2).buildAndRegister();

        // Hard Hammer crafting recipe
        // Centrifuged Ore -> Dust
        ModHandler.addShapelessRecipe(String.format("centrifuged_ore_to_dust_%s", material),
                OreDictUnifier.get(dust, material, crushedMultiplier), 'h', new UnificationEntry(crushedRefined, material));

        processMetalSmelting(prefix, material, property);

        // Chemical Bath recipe (if applicable)
        // Refined Ore -> 2x Dust + Vitriol
        // TODO handle crushed multiplier here
        if (byproductProp.getBathRecipe() != null) {
            byproductProp.getBathRecipe().accept(material);
        } else {
            // Chemical Bath recipe
            // Refined Ore -> Dust + byproduct
            // Only applies if a byproduct in this Material's byproduct
            // list contains either the WASHING_MERCURY or
            // WASHING_PERSULFATE flags
//            Material mercuryByproduct = null;
//            Material persulfateByproduct = null;
//            for (Material propertyByproduct : property.getOreByProducts()) {
//                // find the last byproduct in the list with one of these flags (if any)
//                if (propertyByproduct.hasFlag(WASHING_MERCURY)) mercuryByproduct = propertyByproduct;
//                if (propertyByproduct.hasFlag(WASHING_PERSULFATE)) persulfateByproduct = propertyByproduct;
//            }
//
//            if (mercuryByproduct != null) {
//                CHEMICAL_BATH_RECIPES.recipeBuilder()
//                        .input(crushedRefined, material)
//                        .fluidInputs(Mercury.getFluid(100))
//                        .output(dust, material, 2)
//                        .chancedOutput(dust, mercuryByproduct, 5000, 0)
//                        .duration(400).EUt(VA[LV]).buildAndRegister();
//            }
//
//            if (persulfateByproduct != null) {
//                CHEMICAL_BATH_RECIPES.recipeBuilder()
//                        .input(crushedRefined, material)
//                        .fluidInputs(SodiumPersulfate.getFluid(100))
//                        .output(dust, material)
//                        .chancedOutput(dust, persulfateByproduct, 5000, 0)
//                        .duration(400).EUt(VA[LV]).buildAndRegister();
//
//                CHEMICAL_BATH_RECIPES.recipeBuilder()
//                        .input(crushedRefined, material)
//                        .fluidInputs(PotassiumPersulfate.getFluid(100))
//                        .output(dust, material)
//                        .chancedOutput(dust, persulfateByproduct, 5000, 0)
//                        .duration(400).EUt(VA[LV]).buildAndRegister();
//            }
        }
    }
}
