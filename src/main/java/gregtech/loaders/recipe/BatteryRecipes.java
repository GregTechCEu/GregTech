package gregtech.loaders.recipe;

import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.MetaItems;

import static gregtech.common.items.MetaItems.*;

public class BatteryRecipes {

    public static void init() {


        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(30).EUt(4)
                .input(OrePrefix.dust, Materials.Tantalum)
                .input(OrePrefix.foil, Materials.Manganese)
                .fluidInputs(Materials.Polyethylene.getFluid(144))
                .outputs(MetaItems.BATTERY_RE_ULV_TANTALUM.getStackForm(8))
                .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(BATTERY_RE_LV_CADMIUM.getStackForm())
                .outputs(BATTERY_HULL_LV.getStackForm())
                .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(BATTERY_RE_LV_LITHIUM.getStackForm())
                .outputs(BATTERY_HULL_LV.getStackForm())
                .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(BATTERY_RE_LV_SODIUM.getStackForm())
                .outputs(BATTERY_HULL_LV.getStackForm())
                .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(BATTERY_RE_MV_CADMIUM.getStackForm())
                .outputs(BATTERY_HULL_MV.getStackForm())
                .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(BATTERY_RE_MV_LITHIUM.getStackForm())
                .outputs(BATTERY_HULL_MV.getStackForm())
                .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(BATTERY_RE_MV_SODIUM.getStackForm())
                .outputs(BATTERY_HULL_MV.getStackForm())
                .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(BATTERY_RE_HV_CADMIUM.getStackForm())
                .outputs(BATTERY_HULL_HV.getStackForm())
                .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(BATTERY_RE_HV_LITHIUM.getStackForm())
                .outputs(BATTERY_HULL_HV.getStackForm())
                .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(BATTERY_RE_HV_SODIUM.getStackForm())
                .outputs(BATTERY_HULL_HV.getStackForm())
                .buildAndRegister();

        RecipeMaps.CANNER_RECIPES.recipeBuilder()
                .inputs(BATTERY_HULL_LV.getStackForm())
                .input(OrePrefix.dust, Materials.Cadmium, 2)
                .outputs(BATTERY_RE_LV_CADMIUM.getStackForm())
                .duration(100).EUt(2)
                .buildAndRegister();

        RecipeMaps.CANNER_RECIPES.recipeBuilder()
                .inputs(BATTERY_HULL_LV.getStackForm())
                .input(OrePrefix.dust, Materials.Lithium, 2)
                .outputs(BATTERY_RE_LV_LITHIUM.getStackForm())
                .duration(100).EUt(2)
                .buildAndRegister();

        RecipeMaps.CANNER_RECIPES.recipeBuilder()
                .inputs(BATTERY_HULL_LV.getStackForm())
                .input(OrePrefix.dust, Materials.Sodium, 2)
                .outputs(BATTERY_RE_LV_SODIUM.getStackForm())
                .duration(100).EUt(2)
                .buildAndRegister();

        RecipeMaps.CANNER_RECIPES.recipeBuilder()
                .inputs(BATTERY_HULL_MV.getStackForm())
                .input(OrePrefix.dust, Materials.Cadmium, 8)
                .outputs(BATTERY_RE_MV_CADMIUM.getStackForm())
                .duration(400).EUt(2)
                .buildAndRegister();

        RecipeMaps.CANNER_RECIPES.recipeBuilder()
                .inputs(BATTERY_HULL_MV.getStackForm())
                .input(OrePrefix.dust, Materials.Lithium, 8)
                .outputs(BATTERY_RE_MV_LITHIUM.getStackForm())
                .duration(400).EUt(2)
                .buildAndRegister();

        RecipeMaps.CANNER_RECIPES.recipeBuilder()
                .inputs(BATTERY_HULL_MV.getStackForm())
                .input(OrePrefix.dust, Materials.Sodium, 8)
                .outputs(BATTERY_RE_MV_SODIUM.getStackForm())
                .duration(400).EUt(2)
                .buildAndRegister();

        RecipeMaps.CANNER_RECIPES.recipeBuilder()
                .inputs(BATTERY_HULL_HV.getStackForm())
                .input(OrePrefix.dust, Materials.Cadmium, 16)
                .outputs(BATTERY_RE_HV_CADMIUM.getStackForm())
                .duration(1600).EUt(2)
                .buildAndRegister();

        RecipeMaps.CANNER_RECIPES.recipeBuilder()
                .inputs(BATTERY_HULL_HV.getStackForm())
                .input(OrePrefix.dust, Materials.Lithium, 16)
                .outputs(BATTERY_RE_HV_LITHIUM.getStackForm())
                .duration(1600).EUt(2)
                .buildAndRegister();

        RecipeMaps.CANNER_RECIPES.recipeBuilder()
                .inputs(BATTERY_HULL_HV.getStackForm())
                .input(OrePrefix.dust, Materials.Sodium, 16)
                .outputs(BATTERY_RE_HV_SODIUM.getStackForm())
                .duration(1600).EUt(2)
                .buildAndRegister();
    }
}
