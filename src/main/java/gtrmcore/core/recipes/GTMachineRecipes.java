package gtrmcore.core.recipes;

import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.stack.UnificationEntry;

import gtrmcore.api.unification.material.GTRMMarkerMaterials;
import gtrmcore.api.unification.ore.GTRMOrePrefix;
import gtrmcore.common.metatileentities.GTRMSingleMetaTileEntities;

public class GTMachineRecipes {

    public static void init() {
        // materials();
        // items();
        blocks();
        // tools();
        // end_contents();
    }

    private static void blocks() {
        ModHandler.addShapedRecipe(true, "gregtech.machine.primitive_assembler_bronze",
                GTRMSingleMetaTileEntities.PRIMITIVE_ASSEMBLER_BRONZE.getStackForm(), "   ", "   ", " v ",
                'v', new UnificationEntry(GTRMOrePrefix.valve, GTRMMarkerMaterials.Component.LOW));
    }
}
