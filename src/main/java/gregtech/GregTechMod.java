package gregtech;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.modules.ModuleContainerRegistryEvent;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.core.command.internal.CommandManager;
import gregtech.core.network.internal.NetworkHandler;
import gregtech.modules.GregTechModules;
import gregtech.modules.ModuleManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;

@Mod(modid = GTValues.MODID,
        name = "GregTech",
        acceptedMinecraftVersions = "[1.12,1.13)",
        dependencies = "required:forge@[14.23.5.2847,);"
                + "required-after:codechickenlib@[3.2.3,);"
                + "after:forestry;"
                + "after:jei@[4.15.0,);"
                + "after:crafttweaker@[4.1.20,);")
public class GregTechMod {

    // Hold this so that we can reference non-interface methods without
    // letting the GregTechAPI object see them as immediately.
    private ModuleManager moduleManager;

    public GregTechMod() {
        GregTechAPI.instance = this;
        FluidRegistry.enableUniversalBucket();
        if (FMLCommonHandler.instance().getSide().isClient()) {
            BloomEffectUtil.init();
        }
    }

    @EventHandler
    public void onConstruction(FMLConstructionEvent event) {
        moduleManager = ModuleManager.getInstance();
        GregTechAPI.moduleManager = moduleManager;
        moduleManager.registerContainer(new GregTechModules());

        MinecraftForge.EVENT_BUS.register(moduleManager);
        MinecraftForge.EVENT_BUS.post(new ModuleContainerRegistryEvent());
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        GregTechAPI.networkHandler = NetworkHandler.getInstance();
        moduleManager.setup(event);
        moduleManager.onPreInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        moduleManager.onInit(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        moduleManager.onPostInit(event);
        moduleManager.processIMC(FMLInterModComms.fetchRuntimeMessages(GregTechAPI.instance));
    }

    @EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        moduleManager.onLoadComplete(event);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        CommandManager commandManager = CommandManager.getInstance();
        GregTechAPI.commandManager = commandManager;
        commandManager.registerServerCommand(event);
        moduleManager.onServerStarting(event);
    }

    @EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        moduleManager.onServerStarted(event);
    }

    @EventHandler
    public void serverStopped(FMLServerStoppedEvent event) {
        moduleManager.onServerStopped(event);
    }

    @EventHandler
    public void respondIMC(FMLInterModComms.IMCEvent event) {
        moduleManager.processIMC(event.getMessages());
    }
}
