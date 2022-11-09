package gregtech.loaders.recipe.handlers.oreproc;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.OreProperty;

import static gregtech.api.GTValues.LV;
import static gregtech.api.GTValues.VA;
import static gregtech.api.recipes.RecipeMaps.CHEMICAL_BATH_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.crushedRefined;
import static gregtech.api.unification.ore.OrePrefix.dust;

public class BathRecipeHandler {

    // TODO handle crushed multiplier
    public static void bathBlueVitriol(Material material, OreProperty property, Material byproduct) {
        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedRefined, material)
                .fluidInputs(SulfuricAcid.getFluid(500))
                .output(dust, material, property.getOreMultiplier() * 2)
                .fluidOutputs(BlueVitriol.getFluid(500))
                .fluidOutputs(Hydrogen.getFluid(1000))
                .duration(400).EUt(VA[LV]).buildAndRegister();
    }

    public static void bathGreenVitriol(Material material, OreProperty property, Material byproduct) {
        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedRefined, material)
                .fluidInputs(SulfuricAcid.getFluid(500))
                .output(dust, material, property.getOreMultiplier() * 2)
                .fluidOutputs(GreenVitriol.getFluid(500))
                .fluidOutputs(Hydrogen.getFluid(1000))
                .duration(400).EUt(VA[LV]).buildAndRegister();
    }

    public static void bathRedVitriol(Material material, OreProperty property, Material byproduct) {
        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedRefined, material)
                .fluidInputs(SulfuricAcid.getFluid(500))
                .output(dust, material, property.getOreMultiplier() * 2)
                .fluidOutputs(RedVitriol.getFluid(500))
                .fluidOutputs(Hydrogen.getFluid(1000))
                .duration(400).EUt(VA[LV]).buildAndRegister();
    }

    public static void bathPinkVitriol(Material material, OreProperty property, Material byproduct) {
        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedRefined, material)
                .fluidInputs(SulfuricAcid.getFluid(500))
                .output(dust, material, property.getOreMultiplier() * 2)
                .fluidOutputs(PinkVitriol.getFluid(500))
                .fluidOutputs(Hydrogen.getFluid(1000))
                .duration(400).EUt(VA[LV]).buildAndRegister();
    }

    public static void bathCyanVitriol(Material material, OreProperty property, Material byproduct) {
        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedRefined, material)
                .fluidInputs(SulfuricAcid.getFluid(500))
                .output(dust, material, property.getOreMultiplier() * 2)
                .fluidOutputs(CyanVitriol.getFluid(500))
                .fluidOutputs(Hydrogen.getFluid(1000))
                .duration(400).EUt(VA[LV]).buildAndRegister();
    }

    public static void bathWhiteVitriol(Material material, OreProperty property, Material byproduct) {
        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedRefined, material)
                .fluidInputs(SulfuricAcid.getFluid(500))
                .output(dust, material, property.getOreMultiplier() * 2)
                .fluidOutputs(WhiteVitriol.getFluid(500))
                .fluidOutputs(Hydrogen.getFluid(1000))
                .duration(400).EUt(VA[LV]).buildAndRegister();
    }

    public static void bathGrayVitriol(Material material, OreProperty property, Material byproduct) {
        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedRefined, material)
                .fluidInputs(SulfuricAcid.getFluid(500))
                .output(dust, material, property.getOreMultiplier() * 2)
                .fluidOutputs(GrayVitriol.getFluid(500))
                .fluidOutputs(Hydrogen.getFluid(1000))
                .duration(400).EUt(VA[LV]).buildAndRegister();
    }

    public static void bathClayVitriol(Material material, OreProperty property, Material byproduct) {
        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushedRefined, material)
                .fluidInputs(SulfuricAcid.getFluid(1500))
                .output(dust, material, property.getOreMultiplier() * 2)
                .fluidOutputs(ClayVitriol.getFluid(500))
                .fluidOutputs(Hydrogen.getFluid(3000))
                .duration(400).EUt(VA[LV]).buildAndRegister();
    }
}
