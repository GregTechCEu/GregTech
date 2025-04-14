package gtqt.loaders.recipe.handlers;

import gregtech.api.GTValues;
import gregtech.api.unification.material.MarkerMaterial;

import static gregtech.api.GTValues.ULV;
import static gregtech.api.GTValues.VA;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.unification.ore.OrePrefix.circuit;
import static gregtech.common.metatileentities.MetaTileEntities.*;
import static gtqt.common.metatileentities.GTQTMetaTileEntities.DUAL_EXPORT_HATCH;
import static gtqt.common.metatileentities.GTQTMetaTileEntities.DUAL_IMPORT_HATCH;

public class HatchHandlers {

    public static void init() {
        //Dual Hatch
        for (int i = 0; i < DUAL_IMPORT_HATCH.length; i++) {
            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(ITEM_IMPORT_BUS[i])
                    .input(FLUID_IMPORT_HATCH[i])
                    .input(circuit, MarkerMaterial.create(GTValues.VN[i].toLowerCase()), 4)
                    .output(DUAL_IMPORT_HATCH[i])
                    .duration(100).EUt(VA[ULV + i]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(ITEM_EXPORT_BUS[i])
                    .input(FLUID_EXPORT_HATCH[i])
                    .input(circuit, MarkerMaterial.create(GTValues.VN[i].toLowerCase()), 4)
                    .output(DUAL_EXPORT_HATCH[i])
                    .duration(100).EUt(VA[ULV + i]).buildAndRegister();
        }
    }
}
