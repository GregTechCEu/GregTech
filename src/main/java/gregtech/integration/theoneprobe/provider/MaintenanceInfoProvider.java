package gregtech.integration.theoneprobe.provider;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.metatileentity.multiblock.IMaintenance;
import gregtech.common.ConfigHolder;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class MaintenanceInfoProvider extends CapabilityInfoProvider<IMaintenance> {

    @Override
    protected Capability<IMaintenance> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_MAINTENANCE;
    }

    @Override
    protected void addProbeInfo(IMaintenance capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing sideHit) {
        if (ConfigHolder.machines.enableMaintenance && capability.hasMaintenanceMechanics()) {
            IProbeInfo horizontalPaneMaintenance = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
            if (capability.hasMaintenanceProblems()) {
                horizontalPaneMaintenance.text(TextStyleClass.INFO + "{*gregtech.top.maintenance_broken*}");
            } else {
                horizontalPaneMaintenance.text(TextStyleClass.INFO + "{*gregtech.top.maintenance_fixed*}");
            }
        }
    }

    @Override
    public String getID() {
        return "gregtech:multiblock_maintenance_provider";
    }
}
