package gtrmcore.loaders;

import gtrmcore.common.items.GTRMMetaItems;
import gtrmcore.common.metatileentities.MetaTileEntitiesManager;
import gtrmcore.core.recipes.*;

public class GTRMRecipeManager {

    private GTRMRecipeManager() {}

    public static void preLoad() {}

    public static void load() {
        GTRMMetaItems.init();
        MetaTileEntitiesManager.init();
    }

    public static void loadLow() {
        GTMachineRecipes.init();
        ComponentRecipes.init();
        VanillaOverrideRecipes.init();
        CEUOverrideRecipes.init();
        GTRMRecipes.init();
        GTRMWoodRecipes.init();
        RemoveCEURecipes.init();
    }

    public static void loadLowest() {
        // LowestOverrideRecipeLoader.init();
        //
        // if (Loader.isModLoaded(GTRMValues.MODID_EIO)) {
        // EIORecipeLoader.init();
        // EIOSoulRecipeLoader.init();
        // }
        // if (GTEValues.isModLoadedDEDA()) {
        // DraconicRecipeLoader.init();
        // DraconicUpgradeRecipeLoader.init();
        // }
        // if (Loader.isModLoaded(GTEValues.MODID_GTFO)) {
        // GTFORecipeLoader.init();
        // }
        // if (Loader.isModLoaded(GTEValues.MODID_CHISEL)) {
        // ChiselRecipeLoader.init();
        // }
    }
}
