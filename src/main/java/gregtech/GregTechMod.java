package gregtech;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.modules.ModuleContainerRegistryEvent;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.modules.GregTechModules;
import gregtech.modules.ModuleManager;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Mod(modid = GTValues.MODID,
        name = "GregTech",
        acceptedMinecraftVersions = "[1.12.2,1.13)",
        version = GregTechVersion.VERSION,
        dependencies = "required:forge@[14.23.5.2847,);"
                + "required-after:codechickenlib@[3.2.3,);"
                + "after:forestry;"
                + "after:jei@[4.15.0,);"
                + "after:crafttweaker@[4.1.20,);"
                + "after:groovyscript@[0.1.0,);")
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
        //TODO is setting this needed?
        try {
            Field field = LaunchClassLoader.class.getDeclaredField("DEBUG");
            field.setAccessible(true);

            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            field.setBoolean(null, true);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        moduleManager = ModuleManager.getInstance();
        GregTechAPI.moduleManager = moduleManager;
        moduleManager.registerContainer(new GregTechModules());
        MinecraftForge.EVENT_BUS.post(new ModuleContainerRegistryEvent());
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
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
