package gregtech.integration.hwyla;

import gregtech.api.GTValues;
import gregtech.api.modules.GregTechModule;
import gregtech.integration.IntegrationSubmodule;
import gregtech.integration.hwyla.providers.ElectricContainerDataProvider;
import gregtech.modules.GregTechModules;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.tileentity.TileEntity;

@WailaPlugin
@GregTechModule(
        moduleID = GregTechModules.MODULE_HWYLA,
        containerID = GTValues.MODID,
        modDependencies = GTValues.MODID_HWYLA,
        name = "GregTech HWYLA Integration",
        descriptionKey = "gregtech.modules.hwyla_integration.description"
)
public class HWYLAModule extends IntegrationSubmodule implements IWailaPlugin {

    @Override
    public void register(IWailaRegistrar registrar) {
        registrar.registerBodyProvider(ElectricContainerDataProvider.INSTANCE, TileEntity.class);
        registrar.registerNBTProvider(ElectricContainerDataProvider.INSTANCE, TileEntity.class);
        registrar.addConfig(GTValues.MODID, HWYLAConfigKeys.ENERGY_CONTAINER);
    }
}
