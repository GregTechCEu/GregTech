package gregtech.integration.opencomputers;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.modules.GregTechModule;
import gregtech.integration.IntegrationSubmodule;
import gregtech.integration.IntegrationUtil;
import gregtech.integration.opencomputers.drivers.*;
import gregtech.integration.opencomputers.drivers.specific.DriverConverter;
import gregtech.integration.opencomputers.drivers.specific.DriverFusionReactor;
import gregtech.integration.opencomputers.drivers.specific.DriverPowerSubstation;
import gregtech.integration.opencomputers.drivers.specific.DriverWorldAccelerator;
import gregtech.modules.GregTechModules;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.DriverBlock;

@GregTechModule(
                moduleID = GregTechModules.MODULE_OC,
                containerID = GTValues.MODID,
                modDependencies = GTValues.MODID_OC,
                name = "GregTech OpenComputers Integration",
                description = "OpenComputers Integration Module")
public class OpenComputersModule extends IntegrationSubmodule {

    private static final String MODID_GTCE2OC = "gtce2oc";

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        IntegrationUtil.throwIncompatibilityIfLoaded(MODID_GTCE2OC,
                "All functionality from this mod has been implemented in GregTech CE Unofficial.");
    }

    @Override
    public void init(FMLInitializationEvent event) {
        getLogger().info("Registering OpenComputers Drivers...");
        registerDriver(new DriverEnergyContainer());
        registerDriver(new DriverWorkable());
        registerDriver(new DriverAbstractRecipeLogic());
        registerDriver(new DriverRecipeMapMultiblockController());
        registerDriver(new DriverCoverHolder());
        registerDriver(new DriverSimpleMachine());
        registerDriver(new DriverFusionReactor());
        registerDriver(new DriverWorldAccelerator());
        registerDriver(new DriverConverter());
        registerDriver(new DriverPowerSubstation());
    }

    public static void registerDriver(DriverBlock driver) {
        if (GregTechAPI.moduleManager.isModuleEnabled(GregTechModules.MODULE_OC)) {
            Driver.add(driver);
        }
    }
}
