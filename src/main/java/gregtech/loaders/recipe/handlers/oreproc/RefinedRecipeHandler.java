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
import net.minecraftforge.fluids.FluidStack;

import static gregtech.api.GTValues.LV;
import static gregtech.api.GTValues.VA;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.Materials.Water;
import static gregtech.api.unification.material.info.MaterialFlags.WASHING_MERCURY;
import static gregtech.api.unification.material.info.MaterialFlags.WASHING_PERSULFATE;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.loaders.recipe.handlers.oreproc.OreRecipeHandler.processMetalSmelting;

public class RefinedRecipeHandler {

    public static void processRefined(OrePrefix prefix, Material material, OreProperty property) {
        // Get the byproduct used for this step
        Material byproduct = GTUtility.getOrDefault(property.getOreByProducts(), 0, material);
        OrePrefix byproductPrefix = byproduct.hasProperty(PropertyKey.GEM) ? gem : dust;
        int byproductMultiplier = 1;
        if (byproduct.hasProperty(PropertyKey.ORE))
            byproductMultiplier = byproduct.getProperty(PropertyKey.ORE).getOreMultiplier();

        // Forge Hammer recipe
        // Refined Ore -> Dust
        FORGE_HAMMER_RECIPES.recipeBuilder()
                .input(crushedRefined, material)
                .output(dust, material, property.getOreMultiplier())
                .duration(10).EUt(16).buildAndRegister();

        // Macerator recipe
        // Refined Ore -> 1.33 Dust
        MACERATOR_RECIPES.recipeBuilder()
                .input(crushedRefined, material)
                .output(dust, material, property.getOreMultiplier())
                .chancedOutput(dust, material, property.getOreMultiplier(), 3333, 0)
                .duration(400).EUt(2).buildAndRegister();

        // Hard Hammer crafting recipe
        // Centrifuged Ore -> Dust
        ModHandler.addShapelessRecipe(String.format("centrifuged_ore_to_dust_%s", material),
                OreDictUnifier.get(dust, material, property.getOreMultiplier()), 'h', new UnificationEntry(crushedRefined, material));

        processMetalSmelting(prefix, material, property);

        // Chemical Bath recipe

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedRefined, material)
                .fluidInputs(property.getBathInputStack())
                .output(dustPure, material)
                .chancedOutput(dust, byproduct, byproductMultiplier, 5000, 0)
                .fluidOutputs(property.getBathOutputStacks())
                .duration(256).EUt(64).buildAndRegister();

        /* old
        // Refined Ore -> Purified Dust + Vitriol
        OreProperty byproductProp = byproduct.getProperty(PropertyKey.ORE);
        if (byproductProp != null && byproductProp.getBathRecipe() != null) {
            byproductProp.getBathRecipe().accept(material, property, byproduct);
        } else {
            // Chemical Bath recipe
            // Refined Ore -> Purified Dust + byproduct
            // Only applies if a byproduct in this Material's byproduct
            // list contains either the WASHING_MERCURY or
            // WASHING_PERSULFATE flags

            if (byproduct.hasFlag(WASHING_MERCURY)) {
                CHEMICAL_BATH_RECIPES.recipeBuilder()
                        .input(crushedRefined, material)
                        .fluidInputs(Materials.Mercury.getFluid(100))
                        .output(dustPure, material)
                        .chancedOutput(dust, byproduct, byproductMultiplier, 5000, 0)
                        .duration(256).EUt(VA[LV]).buildAndRegister();
            }

            if (byproduct.hasFlag(WASHING_PERSULFATE)) {
                CHEMICAL_BATH_RECIPES.recipeBuilder()
                        .input(crushedRefined, material)
                        .fluidInputs(Materials.SodiumPersulfate.getFluid(100))
                        .output(dustPure, material)
                        .chancedOutput(dust, byproduct, byproductMultiplier, 5000, 0)
                        .duration(256).EUt(VA[LV]).buildAndRegister();

                CHEMICAL_BATH_RECIPES.recipeBuilder()
                        .input(crushedRefined, material)
                        .fluidInputs(Materials.PotassiumPersulfate.getFluid(100))
                        .output(dustPure, material)
                        .chancedOutput(dust, byproduct, byproductMultiplier, 5000, 0)
                        .duration(256).EUt(VA[LV]).buildAndRegister();
            }

            // Chemical Bath Recipe (fallback)
            CHEMICAL_BATH_RECIPES.recipeBuilder()
                    .input(crushedRefined, material)
                    .fluidInputs(SulfuricAcid.getFluid(250))
                    .output(dustPure, material)
                    .chancedOutput(dust, byproduct, byproductMultiplier, 5000, 0)
                    .duration(400).EUt(VA[LV]).buildAndRegister();
        }

        // Special Bathing Recipes
        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedRefined, Cassiterite)
                .fluidInputs(SulfuricAcid.getFluid(500))
                .output(dustPure, Cassiterite)
                .chancedOutput(crushedRefined, Zinc, 5000, 0)
                .fluidOutputs(WhiteVitriol.getFluid(500))
                .fluidOutputs(Hydrogen.getFluid(1000))
                .duration(400).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedRefined, CassiteriteSand)
                .fluidInputs(SulfuricAcid.getFluid(500))
                .output(dustPure, CassiteriteSand)
                .chancedOutput(crushedRefined, Zinc, 5000, 0)
                .fluidOutputs(WhiteVitriol.getFluid(500))
                .fluidOutputs(Hydrogen.getFluid(1000))
                .duration(400).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedRefined, Tin)
                .fluidInputs(SulfuricAcid.getFluid(500))
                .output(dustPure, Tin)
                .chancedOutput(crushedRefined, Zinc, 5000, 0)
                .fluidOutputs(WhiteVitriol.getFluid(500))
                .fluidOutputs(Hydrogen.getFluid(1000))
                .duration(400).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedRefined, Copper)
                .fluidInputs(AquaRegia.getFluid(6000))
                .output(dustPure, Copper)
                .chancedOutput(crushedRefined, Cobalt, 5000, 0)
                .fluidOutputs(ChloroauricAcid.getFluid(500))
                .fluidOutputs(NitrogenDioxide.getFluid(2000))
                .fluidOutputs(Water.getFluid(2000))
                .duration(400).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedRefined, Gold)
                .fluidInputs(AquaRegia.getFluid(6000))
                .output(dustPure, Gold)
                .chancedOutput(crushedRefined, Copper, 5000, 0)
                .fluidOutputs(ChloroauricAcid.getFluid(500))
                .fluidOutputs(NitrogenDioxide.getFluid(2000))
                .fluidOutputs(Water.getFluid(2000))
                .duration(400).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedRefined, Cooperite)
                .fluidInputs(AquaRegia.getFluid(9000))
                .output(dustPure, Cooperite)
                .chancedOutput(dust, PlatinumGroupSludge, 2, 5000, 0)
                .fluidOutputs(ChloroplatinicAcid.getFluid(500))
                .fluidOutputs(NitrogenDioxide.getFluid(3000))
                .fluidOutputs(Water.getFluid(3000))
                .duration(400).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedRefined, Platinum)
                .fluidInputs(AquaRegia.getFluid(9000))
                .output(dustPure, Platinum)
                .chancedOutput(dust, PlatinumGroupSludge, 2, 5000, 0)
                .fluidOutputs(ChloroplatinicAcid.getFluid(500))
                .fluidOutputs(NitrogenDioxide.getFluid(3000))
                .fluidOutputs(Water.getFluid(3000))
                .duration(400).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedRefined, Palladium)
                .fluidInputs(AquaRegia.getFluid(9000))
                .output(dustPure, Palladium)
                .chancedOutput(dust, PlatinumGroupSludge, 2, 5000, 0)
                .fluidOutputs(ChloroplatinicAcid.getFluid(500))
                .fluidOutputs(NitrogenDioxide.getFluid(3000))
                .fluidOutputs(Water.getFluid(3000))
                .duration(400).EUt(VA[LV]).buildAndRegister();


         */
    }
}
