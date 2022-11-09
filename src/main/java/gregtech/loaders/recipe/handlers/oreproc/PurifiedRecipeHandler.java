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

import static gregtech.api.GTValues.MV;
import static gregtech.api.GTValues.VA;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.info.MaterialFlags.MAGNETIC_ORE;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.loaders.recipe.handlers.oreproc.OreRecipeHandler.processMetalSmelting;

public class PurifiedRecipeHandler {

    public static void processPurified(OrePrefix prefix, Material material, OreProperty property) {
        boolean chancePerTier = ConfigHolder.recipes.oreByproductChancePerTier;
        // Get the byproducts used for this step
        Material primaryByproduct = GTUtility.selectItemInList(1, material, property.getOreByProducts(), Material.class);
        OrePrefix primaryByproductPrefix = primaryByproduct.hasProperty(PropertyKey.GEM) ? gem : dust;
        int primaryByproductMultiplier = 1;
        if (primaryByproduct.hasProperty(PropertyKey.ORE))
            primaryByproductMultiplier = primaryByproduct.getProperty(PropertyKey.ORE).getOreMultiplier();
        Material secondaryByproduct = GTUtility.selectItemInList(2, material, property.getOreByProducts(), Material.class);
        OrePrefix secondaryByproductPrefix = secondaryByproduct.hasProperty(PropertyKey.GEM) ? gem : dust;
        int secondaryByproductMultiplier = 1;
        if (secondaryByproduct.hasProperty(PropertyKey.ORE))
            secondaryByproductMultiplier = secondaryByproduct.getProperty(PropertyKey.ORE).getOreMultiplier();

        // Forge Hammer recipe
        // Purified Ore -> Dust
        FORGE_HAMMER_RECIPES.recipeBuilder()
                .input(crushedPurified, material)
                .output(dust, material, property.getOreMultiplier())
                .duration(10).EUt(16).buildAndRegister();

        // Macerator recipe
        // Purified Ore -> Dust
        MACERATOR_RECIPES.recipeBuilder()
                .input(crushedPurified, material)
                .output(dust, material, property.getOreMultiplier())
                .chancedOutput(dust, material, primaryByproductMultiplier, 2500, 0)
                .chancedOutput(primaryByproductPrefix, primaryByproduct, 2000, chancePerTier ? 500 : 0)
                .duration(400).EUt(2).buildAndRegister();

        // Purified Ore -> Refined Ore
        if (material.hasProperty(PropertyKey.GEM)) {
            // Gems go in the Sifter
            // TODO add gem quality?
            SIFTER_RECIPES.recipeBuilder()
                    .input(crushedPurified, material)
                    .output(crushedRefined, material)
                    .chancedOutput(primaryByproductPrefix, primaryByproduct, primaryByproductMultiplier, 2000, 0)
                    .chancedOutput(secondaryByproductPrefix, secondaryByproduct, secondaryByproductMultiplier,2000, 0)
                    .duration(400).EUt(VA[MV]).buildAndRegister();
        } else if (material.hasFlag(MAGNETIC_ORE) || primaryByproduct.hasFlag(MAGNETIC_ORE)) {
            // Magnetic Materials or Byproducts go in the Magnetic Separator
            ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder()
                    .input(crushedPurified, material)
                    .output(crushedRefined, material)
                    .chancedOutput(primaryByproductPrefix, primaryByproduct, primaryByproductMultiplier, 2000, 0)
                    .chancedOutput(secondaryByproductPrefix, secondaryByproduct, secondaryByproductMultiplier,2000, 0)
                    .duration(400).EUt(VA[MV]).buildAndRegister();
        } else {
            // Anything else goes in the Thermal Centrifuge
            THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
                    .input(crushedPurified, material)
                    .output(crushedRefined, material)
                    .chancedOutput(primaryByproductPrefix, primaryByproduct, primaryByproductMultiplier, 2000, 0)
                    .chancedOutput(secondaryByproductPrefix, secondaryByproduct, secondaryByproductMultiplier,2000, 0)
                    .duration(400).EUt(VA[MV]).buildAndRegister();
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
                OreDictUnifier.get(dust, material, property.getOreMultiplier()), 'h', new UnificationEntry(crushedPurified, material));

        processMetalSmelting(prefix, material, property);
    }
}
