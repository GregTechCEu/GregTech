package gregtech.integration;

import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import crazypants.enderio.api.farm.IFertilizer;
import crazypants.enderio.base.farming.fertilizer.Bonemeal;
import gregtech.api.GTValues;
import gregtech.api.modules.GregTechModule;
import gregtech.common.items.MetaItems;
import gregtech.modules.BaseGregTechModule;
import gregtech.modules.GregTechModules;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

@GregTechModule(
        moduleID = GregTechModules.MODULE_INTEGRATION,
        containerID = GTValues.MODID,
        name = "GregTech Mod Integration",
        descriptionKey = "gregtech.modules.integration.description"
)
public class IntegrationModule extends BaseGregTechModule {

    public static final Logger logger = LogManager.getLogger("GregTech Mod Integration");

    @Nonnull
    @Override
    public Logger getLogger() {
        return logger;
    }
    @Nonnull
    @Override
    public List<Class<?>> getEventBusSubscribers() {
        return Collections.singletonList(IntegrationModule.class);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        if (Loader.isModLoaded(GTValues.MODID_IE)) {
            BelljarHandler.registerBasicItemFertilizer(MetaItems.FERTILIZER.getStackForm(), 1.25f);
            logger.info("Registered Immersive Engineering Compat");
        }
    }

    @Optional.Method(modid = GTValues.MODID_EIO)
    @SubscribeEvent
    public static void registerFertilizer(@Nonnull RegistryEvent.Register<IFertilizer> event) {
        event.getRegistry().register(new Bonemeal(MetaItems.FERTILIZER.getStackForm()));
        logger.info("Registered EnderIO Compat");
    }
}
