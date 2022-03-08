package gregtech.loaders.recipe.handlers.oreproc;

import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.WASHING_MERCURY;
import static gregtech.api.unification.material.info.MaterialFlags.WASHING_PERSULFATE;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.loaders.recipe.handlers.oreproc.OreRecipeHandler.processMetalSmelting;

public class CrushedRecipeHandler {

    private static final List<Material> checkedMaterials = new ArrayList<>();

    public static void processCrushed(OrePrefix prefix, Material material, OreProperty property) {
        // Get the byproducts to use for this step
        Material primaryByproduct = GTUtility.selectItemInList(0, material, property.getOreByProducts(), Material.class);
        Material secondaryByproduct = GTUtility.selectItemInList(1, material, property.getOreByProducts(), Material.class);

        // TODO Remove these two
        Material tertiaryByproduct = GTUtility.selectItemInList(2, material, property.getOreByProducts(), Material.class);
        Material quaternaryByproduct = GTUtility.selectItemInList(3, material, property.getOreByProducts(), Material.class);

        // Check all byproducts for needing an Ore Property for debugging TODO Remove this!
        if (!primaryByproduct.hasProperty(PropertyKey.ORE) && !checkedMaterials.contains(primaryByproduct)) {
            GTLog.logger.info("Material {} would need an Ore!", primaryByproduct);
            checkedMaterials.add(primaryByproduct);
        }
        if (!secondaryByproduct.hasProperty(PropertyKey.ORE) && !checkedMaterials.contains(secondaryByproduct)) {
            GTLog.logger.info("Material {} would need an Ore!", secondaryByproduct);
            checkedMaterials.add(secondaryByproduct);
        }
        if (!tertiaryByproduct.hasProperty(PropertyKey.ORE) && !checkedMaterials.contains(tertiaryByproduct)) {
            GTLog.logger.info("Material {} would need an Ore!", tertiaryByproduct);
            checkedMaterials.add(tertiaryByproduct);
        }
        if (!quaternaryByproduct.hasProperty(PropertyKey.ORE) && !checkedMaterials.contains(quaternaryByproduct)) {
            GTLog.logger.info("Material {} would need an Ore!", quaternaryByproduct);
            checkedMaterials.add(quaternaryByproduct);
        }

        // TODO Should this cause any effect on byproducts?
        int crushedMultiplier = (int) (crushed.getMaterialAmount(material) / M);

        // Forge Hammer recipe
        // Crushed Ore -> Impure Dust
        FORGE_HAMMER_RECIPES.recipeBuilder()
                .input(crushed, material)
                .output(dustImpure, material, crushedMultiplier)
                .duration(10).EUt(16).buildAndRegister();

        // Macerator recipe
        // Crushed Ore -> Impure Dust (with byproduct)
        MACERATOR_RECIPES.recipeBuilder()
                .input(crushed, material)
                .output(dustImpure, material, crushedMultiplier)
                .chancedOutput(dust, primaryByproduct, 1400, 850)
                .output(dust, Stone)
                .duration(400).EUt(2).buildAndRegister();

        // Ore Washer recipes
        // Crushed Ore -> Purified Ore
        ORE_WASHER_RECIPES.recipeBuilder()
                .input(crushed, material)
                .fluidInputs(Water.getFluid(1000))
                .output(crushedPurified, material)
                .output(dustTiny, primaryByproduct)
                .output(dust, Stone)
                .duration(400).EUt(16).buildAndRegister();

        // Thermal Centrifuge recipe
        // Crushed Ore -> Refined Ore
        THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
                .input(crushed, material)
                .output(crushedRefined, material)
                .output(dustTiny, secondaryByproduct, 3)
                .output(dust, Stone)
                .duration(400).EUt(VA[LV]).buildAndRegister();

        // Chemical Bath recipe
        // Crushed Ore -> Purified Ore + Purified Ore Byproduct
        // Only applies if a byproduct in this Material's byproduct
        // list contains either the WASHING_MERCURY or
        // WASHING_PERSULFATE flags
        Material mercuryByproduct = null;
        Material persulfateByproduct = null;
        for (Material byproduct : property.getOreByProducts()) {
            // find the first byproduct in the list with one of these flags (if any)
            if (byproduct.hasFlag(WASHING_MERCURY)) mercuryByproduct = byproduct;
            if (byproduct.hasFlag(WASHING_PERSULFATE)) persulfateByproduct = byproduct;
        }

        if (mercuryByproduct != null) {
            CHEMICAL_BATH_RECIPES.recipeBuilder()
                    .input(crushed, material)
                    .fluidInputs(Mercury.getFluid(100))
                    .output(crushedPurified, material)
                    .output(crushedPurified, mercuryByproduct)
                    .output(dust, Stone)
                    .duration(400).EUt(VA[LV]).buildAndRegister();
        }

        if (persulfateByproduct != null) {
            CHEMICAL_BATH_RECIPES.recipeBuilder()
                    .input(crushed, material)
                    .fluidInputs(SodiumPersulfate.getFluid(100))
                    .output(crushedPurified, material)
                    .output(crushedPurified, persulfateByproduct)
                    .output(dust, Stone)
                    .duration(400).EUt(VA[LV]).buildAndRegister();

            CHEMICAL_BATH_RECIPES.recipeBuilder()
                    .input(crushed, material)
                    .fluidInputs(PotassiumPersulfate.getFluid(100))
                    .output(crushedPurified, material)
                    .output(crushedPurified, persulfateByproduct)
                    .output(dust, Stone)
                    .duration(400).EUt(VA[LV]).buildAndRegister();
        }

        // Hard Hammer crafting recipe
        // Crushed Ore -> Impure Dust
        ModHandler.addShapelessRecipe(String.format("crushed_ore_to_dust_%s", material),
                OreDictUnifier.get(dustImpure, material, crushedMultiplier), 'h', new UnificationEntry(crushed, material));

        processMetalSmelting(prefix, material, property);
    }
}
