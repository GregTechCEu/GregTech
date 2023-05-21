package gregtech.worldgen;

import gregtech.api.GTValues;
import gregtech.api.modules.GregTechModule;
import gregtech.modules.BaseGregTechModule;
import gregtech.modules.GregTechModules;
import gregtech.worldgen.config.internal.GTWorldgenDefaults;
import gregtech.worldgen.terrain.GTTerrainGenManager;
import gregtech.worldgen.terrain.VanillaTerrainHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

@GregTechModule(
        moduleID = GregTechModules.MODULE_WORLDGEN,
        containerID = GTValues.MODID,
        name = "GregTech Worldgen",
        descriptionKey = "gregtech.modules.worldgen.description"
)
@SuppressWarnings("unused")
public final class WorldgenModule extends BaseGregTechModule {

    public static final Logger logger = LogManager.getLogger("GregTech Worldgen");

    @Nonnull
    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        GTWorldgenDefaults.init();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        logger.info("Initializing Terrain Generation");
        GTTerrainGenManager.startup();
        MinecraftForge.ORE_GEN_BUS.register(VanillaTerrainHandler.class);
        logger.info("Terrain Generation Initialized");
    }

    @Override
    public void serverStopped(FMLServerStoppedEvent event) {
        GTTerrainGenManager.terminate();
    }
}
