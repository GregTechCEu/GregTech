package gtqt.loaders.recipe.handlers;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Materials;

import static gregtech.api.GTValues.ULV;
import static gregtech.api.GTValues.VA;
import static gregtech.api.recipes.RecipeMaps.CANNER_RECIPES;
import static gregtech.api.unification.material.Materials.Acetone;
import static gtqt.common.items.GTQTMetaItems.*;

public class SprayHandlers {
    public static void init() {
        for (int i = 0; i < Materials.CHEMICAL_DYES.length; i++) {
            CANNER_RECIPES.recipeBuilder()
                    .inputs(ENDLESS_SPRAY_EMPTY.getStackForm())
                    .fluidInputs(Materials.CHEMICAL_DYES[i].getFluid(GTValues.L * 4))
                    .outputs(ENDLESS_SPRAY_CAN_DYES[i].getStackForm())
                    .EUt(VA[ULV]).duration(200)
                    .buildAndRegister();

        }
        CANNER_RECIPES.recipeBuilder()
                .input(ENDLESS_SPRAY_EMPTY)
                .fluidInputs(Acetone.getFluid(1000))
                .output(ENDLESS_SPRAY_SOLVENT)
                .EUt(VA[ULV]).duration(200)
                .buildAndRegister();
    }
}
