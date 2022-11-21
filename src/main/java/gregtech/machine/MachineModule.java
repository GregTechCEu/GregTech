package gregtech.machine;

import gregtech.api.GTValues;
import gregtech.api.module.GregTechModule;
import gregtech.machine.sound.MachineSounds;
import gregtech.module.BaseGregTechModule;
import gregtech.module.GregTechModules;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

@GregTechModule(
        moduleID = GregTechModules.MODULE_MACHINE,
        containerID = GTValues.MODID,
        name = "GregTech Machines",
        descriptionKey = "gregtech.modules.machine.description"
)
public class MachineModule extends BaseGregTechModule {

    public static final Logger logger = LogManager.getLogger("GregTech Machines");

    @Nonnull
    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        MachineSounds.register();
    }
}
