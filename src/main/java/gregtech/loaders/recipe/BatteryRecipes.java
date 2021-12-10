package gregtech.loaders.recipe;

import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.stack.UnificationEntry;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.*;

public class BatteryRecipes {

    public static void init() {

        // Tantalum Battery (since it doesn't fit elsewhere)
        ASSEMBLER_RECIPES.recipeBuilder().duration(30).EUt(4)
                .input(dust, Tantalum)
                .input(foil, Manganese)
                .fluidInputs(Polyethylene.getFluid(144))
                .outputs(BATTERY_ULV_TANTALUM.getStackForm(8))
                .buildAndRegister();

        // Battery Hull Recipes

        // LV
        ModHandler.addShapedRecipe("battery_hull_lv", BATTERY_HULL_LV.getStackForm(),
                "C", "P", "P",
                'C', new UnificationEntry(cableGtSingle, Tin),
                'P', new UnificationEntry(plate, BatteryAlloy));

        ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(1)
                .input(cableGtSingle, Tin)
                .input(plate, BatteryAlloy)
                .fluidInputs(Polyethylene.getFluid(144))
                .output(BATTERY_HULL_LV)
                .buildAndRegister();

        // MV
        ModHandler.addShapedRecipe("battery_hull_mv", BATTERY_HULL_MV.getStackForm(),
                "C C", "PPP", "PPP",
                'C', new UnificationEntry(cableGtSingle, Copper),
                'P', new UnificationEntry(plate, BatteryAlloy));

        ASSEMBLER_RECIPES.recipeBuilder().duration(100).EUt(2)
                .input(cableGtSingle, Copper, 2)
                .input(plate, BatteryAlloy, 3)
                .fluidInputs(Polyethylene.getFluid(432))
                .output(BATTERY_HULL_MV)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(2)
                .input(cableGtSingle, AnnealedCopper, 2)
                .input(plate, BatteryAlloy, 3)
                .fluidInputs(Polyethylene.getFluid(432))
                .output(BATTERY_HULL_MV)
                .buildAndRegister();

        // HV
        ASSEMBLER_RECIPES.recipeBuilder().duration(300).EUt(4)
                .input(cableGtSingle, Gold, 4)
                .input(plate, BatteryAlloy, 9)
                .fluidInputs(Polyethylene.getFluid(1296))
                .output(BATTERY_HULL_HV)
                .buildAndRegister();

        // EV
        ASSEMBLER_RECIPES.recipeBuilder().duration(100).EUt(VA[HV])
                .input(cableGtSingle, Aluminium, 2)
                .input(plate, BlueSteel, 2)
                .fluidInputs(Polytetrafluoroethylene.getFluid(144))
                .output(BATTERY_HULL_SMALL_VANADIUM)
                .buildAndRegister();

        // IV
        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV])
                .input(cableGtSingle, Platinum, 2)
                .input(plate, RoseGold, 6)
                .fluidInputs(Polytetrafluoroethylene.getFluid(288))
                .output(BATTERY_HULL_MEDIUM_VANADIUM)
                .buildAndRegister();

        // LuV
        ASSEMBLER_RECIPES.recipeBuilder().duration(300).EUt(VA[IV])
                .input(cableGtSingle, NiobiumTitanium, 2)
                .input(plate, RedSteel, 18)
                .fluidInputs(Polybenzimidazole.getFluid(144))
                .output(BATTERY_HULL_LARGE_VANADIUM)
                .buildAndRegister();

        // ZPM
        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[LuV])
                .input(cableGtSingle, Naquadah, 2)
                .input(plate, Europium, 6)
                .fluidInputs(Polybenzimidazole.getFluid(288))
                .output(BATTERY_HULL_MEDIUM_NAQUADRIA)
                .buildAndRegister();

        // UV
        ASSEMBLER_RECIPES.recipeBuilder().duration(300).EUt(VA[ZPM])
                .input(cableGtSingle, NaquadahAlloy, 2)
                .input(plate, Americium, 18)
                .fluidInputs(Polybenzimidazole.getFluid(576))
                .output(BATTERY_HULL_LARGE_NAQUADRIA)
                .buildAndRegister();

        // Battery Filling Recipes

        // LV
        CANNER_RECIPES.recipeBuilder().duration(100).EUt(2)
                .input(BATTERY_HULL_LV)
                .input(dust, Cadmium, 2)
                .output(BATTERY_LV_CADMIUM)
                .buildAndRegister();

        CANNER_RECIPES.recipeBuilder().duration(100).EUt(2)
                .input(BATTERY_HULL_LV)
                .input(dust, Lithium, 2)
                .output(BATTERY_LV_LITHIUM)
                .buildAndRegister();

        CANNER_RECIPES.recipeBuilder().duration(100).EUt(2)
                .input(BATTERY_HULL_LV)
                .input(dust, Sodium, 2)
                .output(BATTERY_LV_SODIUM)
                .buildAndRegister();

        // MV
        CANNER_RECIPES.recipeBuilder().duration(400).EUt(2)
                .input(BATTERY_HULL_MV)
                .input(dust, Cadmium, 8)
                .output(BATTERY_MV_CADMIUM)
                .buildAndRegister();

        CANNER_RECIPES.recipeBuilder().duration(400).EUt(2)
                .input(BATTERY_HULL_MV)
                .input(dust, Lithium, 8)
                .output(BATTERY_MV_LITHIUM)
                .buildAndRegister();

        CANNER_RECIPES.recipeBuilder().duration(400).EUt(2)
                .input(BATTERY_HULL_MV)
                .input(dust, Sodium, 8)
                .output(BATTERY_MV_SODIUM)
                .buildAndRegister();

        // HV
        CANNER_RECIPES.recipeBuilder().duration(1600).EUt(2)
                .input(BATTERY_HULL_HV)
                .input(dust, Cadmium, 16)
                .output(BATTERY_HV_CADMIUM)
                .buildAndRegister();

        CANNER_RECIPES.recipeBuilder().duration(1600).EUt(2)
                .input(BATTERY_HULL_HV)
                .input(dust, Lithium, 16)
                .output(BATTERY_HV_LITHIUM)
                .buildAndRegister();

        CANNER_RECIPES.recipeBuilder().duration(1600).EUt(2)
                .input(BATTERY_HULL_HV)
                .input(dust, Sodium, 16)
                .output(BATTERY_HV_SODIUM)
                .buildAndRegister();

        // EV
        CANNER_RECIPES.recipeBuilder().duration(100).EUt(VA[HV])
                .input(BATTERY_HULL_SMALL_VANADIUM)
                .input(dust, Vanadium, 2)
                .output(BATTERY_EV_VANADIUM)
                .buildAndRegister();

        // IV
        CANNER_RECIPES.recipeBuilder().duration(150).EUt(1024)
                .input(BATTERY_HULL_MEDIUM_VANADIUM)
                .input(dust, Vanadium, 8)
                .output(BATTERY_IV_VANADIUM)
                .buildAndRegister();

        // LuV
        CANNER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV])
                .input(BATTERY_HULL_LARGE_VANADIUM)
                .input(dust, Vanadium, 16)
                .output(BATTERY_LUV_VANADIUM)
                .buildAndRegister();

        // ZPM
        CANNER_RECIPES.recipeBuilder().duration(250).EUt(4096)
                .input(BATTERY_HULL_MEDIUM_NAQUADRIA)
                .input(dust, Naquadria, 8)
                .output(BATTERY_ZPM_NAQUADRIA)
                .buildAndRegister();

        // UV
        CANNER_RECIPES.recipeBuilder().duration(300).EUt(VA[IV])
                .input(BATTERY_HULL_LARGE_NAQUADRIA)
                .input(dust, Naquadria, 16)
                .output(BATTERY_UV_NAQUADRIA)
                .buildAndRegister();


        // Battery Recycling Recipes
        EXTRACTOR_RECIPES.recipeBuilder().input(BATTERY_LV_CADMIUM).output(BATTERY_HULL_LV).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().input(BATTERY_LV_LITHIUM).output(BATTERY_HULL_LV).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().input(BATTERY_LV_SODIUM).output(BATTERY_HULL_LV).buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().input(BATTERY_MV_CADMIUM).output(BATTERY_HULL_MV).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().input(BATTERY_MV_LITHIUM).output(BATTERY_HULL_MV).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().input(BATTERY_MV_SODIUM).output(BATTERY_HULL_MV).buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().input(BATTERY_HV_CADMIUM).output(BATTERY_HULL_HV).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().input(BATTERY_HV_LITHIUM).output(BATTERY_HULL_HV).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().input(BATTERY_HV_SODIUM).output(BATTERY_HULL_HV).buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().input(BATTERY_EV_VANADIUM).output(BATTERY_HULL_SMALL_VANADIUM).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().input(BATTERY_IV_VANADIUM).output(BATTERY_HULL_MEDIUM_VANADIUM).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().input(BATTERY_LUV_VANADIUM).output(BATTERY_HULL_LARGE_VANADIUM).buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().input(BATTERY_ZPM_NAQUADRIA).output(BATTERY_HULL_MEDIUM_NAQUADRIA).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().input(BATTERY_UV_NAQUADRIA).output(BATTERY_HULL_LARGE_NAQUADRIA).buildAndRegister();
    }
}
