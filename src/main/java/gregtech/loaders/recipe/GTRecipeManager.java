package gregtech.loaders.recipe;

import gregtech.api.GregTechAPI;
import gregtech.api.event.MaterialInfoEvent;
import gregtech.core.unification.ore.internal.OreProcessorManagerImpl;
import gregtech.loaders.recipe.handlers.DecompositionRecipeHandler;
import gregtech.loaders.recipe.handlers.ToolRecipeHandler;

import net.minecraftforge.common.MinecraftForge;

public final class GTRecipeManager {

    private GTRecipeManager() {/**/}

    public static void preLoad() {
        ToolRecipeHandler.initializeMetaItems();
    }

    public static void load() {
        MachineRecipeLoader.init();
        CraftingRecipeLoader.init();
        MetaTileEntityLoader.init();
        MetaTileEntityMachineRecipeLoader.init();
        OreProcessorManagerImpl oreProcessorManager = (OreProcessorManagerImpl) GregTechAPI.oreProcessorManager;
        oreProcessorManager.startRegistration();
        oreProcessorManager.startRemoval();
    }

    public static void loadLatest() {
        MinecraftForge.EVENT_BUS.post(new MaterialInfoEvent());
        DecompositionRecipeHandler.runRecipeGeneration();
        RecyclingRecipes.init();
    }
}
