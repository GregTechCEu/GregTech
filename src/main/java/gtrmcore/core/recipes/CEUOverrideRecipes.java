package gtrmcore.core.recipes;

import gregtech.api.recipes.ModHandler;
import gregtech.common.metatileentities.MetaTileEntities;

import gtrmcore.common.items.GTRMItems;

public class CEUOverrideRecipes {

    public static void init() {
        materials();
        items();
        blocks();
    }

    private static void materials() {}

    private static void items() {}

    private static void blocks() {
        ModHandler.addShapedRecipe(true, "workbench_bronze",
                MetaTileEntities.WORKBENCH.getStackForm(), "HP", "PS",
                'H', GTRMItems.WOODEN_HARD_HAMMER,
                'S', GTRMItems.COBBLESTONE_SAW,
                'P', "plankWood");
    }
}
