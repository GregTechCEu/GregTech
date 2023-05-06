package gregtech.loaders.recipe.handlers.oreproc;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.OreProperty;

import static gregtech.api.GTValues.LV;
import static gregtech.api.GTValues.VA;
import static gregtech.api.recipes.RecipeMaps.CHEMICAL_BATH_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class BathRecipeHandler {

    // TODO handle crushed multiplier
    public static void bathBlueVitriol(Material material, OreProperty property, Material byproduct) {
        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(purified, material)
                .fluidInputs(SulfuricAcid.getFluid(1000))
                .output(refined, material, property.getOreMultiplier())
                .fluidOutputs(BlueVitriol.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(2000))
                .duration(400).EUt(VA[LV]).buildAndRegister();
    }

    public static void bathGreenVitriol(Material material, OreProperty property, Material byproduct) {
        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(purified, material)
                .fluidInputs(SulfuricAcid.getFluid(1000))
                .output(refined, material, property.getOreMultiplier())
                .fluidOutputs(GreenVitriol.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(2000))
                .duration(400).EUt(VA[LV]).buildAndRegister();
    }

    public static void bathRedVitriol(Material material, OreProperty property, Material byproduct) {
        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(purified, material)
                .fluidInputs(SulfuricAcid.getFluid(1000))
                .output(refined, material, property.getOreMultiplier())
                .fluidOutputs(RedVitriol.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(2000))
                .duration(400).EUt(VA[LV]).buildAndRegister();
    }

    public static void bathPinkVitriol(Material material, OreProperty property, Material byproduct) {
        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(purified, material)
                .fluidInputs(SulfuricAcid.getFluid(1000))
                .output(refined, material, property.getOreMultiplier())
                .fluidOutputs(PinkVitriol.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(2000))
                .duration(400).EUt(VA[LV]).buildAndRegister();
    }

    public static void bathCyanVitriol(Material material, OreProperty property, Material byproduct) {
        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(purified, material)
                .fluidInputs(SulfuricAcid.getFluid(1000))
                .output(refined, material, property.getOreMultiplier())
                .fluidOutputs(CyanVitriol.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(2000))
                .duration(400).EUt(VA[LV]).buildAndRegister();
    }

    public static void bathWhiteVitriol(Material material, OreProperty property, Material byproduct) {
        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(purified, material)
                .fluidInputs(SulfuricAcid.getFluid(1000))
                .output(refined, material, property.getOreMultiplier())
                .fluidOutputs(WhiteVitriol.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(2000))
                .duration(400).EUt(VA[LV]).buildAndRegister();
    }

    public static void bathGrayVitriol(Material material, OreProperty property, Material byproduct) {
        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(purified, material)
                .fluidInputs(SulfuricAcid.getFluid(1000))
                .output(refined, material, property.getOreMultiplier())
                .fluidOutputs(GrayVitriol.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(2000))
                .duration(400).EUt(VA[LV]).buildAndRegister();
    }

    public static void bathClayVitriol(Material material, OreProperty property, Material byproduct) {
        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(purified, material)
                .fluidInputs(SulfuricAcid.getFluid(1500))
                .output(refined, material, property.getOreMultiplier())
                .fluidOutputs(ClayVitriol.getFluid(500))
                .fluidOutputs(Hydrogen.getFluid(3000))
                .duration(400).EUt(VA[LV]).buildAndRegister();
    }
}
