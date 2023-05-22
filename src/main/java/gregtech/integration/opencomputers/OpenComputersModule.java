package gregtech.integration.opencomputers;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.modules.GregTechModule;
import gregtech.integration.IntegrationSubmodule;
import gregtech.integration.IntegrationUtil;
import gregtech.integration.opencomputers.drivers.*;
import gregtech.integration.opencomputers.drivers.specific.*;
import gregtech.modules.GregTechModules;
import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.DriverBlock;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@GregTechModule(
        moduleID = GregTechModules.MODULE_OC,
        containerID = GTValues.MODID,
        modDependencies = GTValues.MODID_OC,
        name = "GregTech OpenComputers Integration",
        descriptionKey = "gregtech.modules.oc_integration.description"
)
public class OpenComputersModule extends IntegrationSubmodule {

    private static final String MODID_GTCE2OC = "gtce2oc";

    @Override
    public void init(FMLInitializationEvent event) {
        IntegrationUtil.throwIncompatibilityIfLoaded(MODID_GTCE2OC,
                "All functionality from this mod has been implemented in GregTech CE Unofficial.");

        getLogger().info("Registering OpenComputers Drivers...");
        registerDriver(new DriverEnergyContainer());
        registerDriver(new DriverWorkable());
        registerDriver(new DriverAbstractRecipeLogic());
        registerDriver(new DriverRecipeMapMultiblockController());
        registerDriver(new DriverICoverable());
        registerDriver(new DriverSimpleMachine());
        registerDriver(new DriverFusionReactor());
        registerDriver(new DriverWorldAccelerator());
        registerDriver(new DriverConverter());
    }

    public static void registerDriver(DriverBlock driver) {
        if (GregTechAPI.moduleManager.isModuleEnabled(GregTechModules.MODULE_OC)) {
            Driver.add(driver);
        }
    }
}
