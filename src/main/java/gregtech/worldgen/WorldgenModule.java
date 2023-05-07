package gregtech.worldgen;

import gregtech.api.GTValues;
import gregtech.api.modules.GregTechModule;
import gregtech.modules.BaseGregTechModule;
import gregtech.modules.GregTechModules;
import gregtech.worldgen.terrain.GTTerrainGenManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

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

    @Nonnull
    @Override
    public List<Class<?>> getEventBusSubscribers() {
        return Collections.singletonList(GTTerrainGenManager.class);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        logger.info("Initializing Terrain Generation");
        GTTerrainGenManager.startup();
        logger.info("Terrain Generation Initialized");
    }

    @Override
    public void serverStopped(FMLServerStoppedEvent event) {
        GTTerrainGenManager.terminate();
    }
}
