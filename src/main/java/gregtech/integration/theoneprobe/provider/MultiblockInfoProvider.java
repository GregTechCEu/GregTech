package gregtech.integration.theoneprobe.provider;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IMultiblockController;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class MultiblockInfoProvider extends CapabilityInfoProvider<IMultiblockController> {

    @Override
    protected Capability<IMultiblockController> getCapability() {
        return GregtechCapabilities.CAPABILITY_MULTIBLOCK_CONTROLLER;
    }

    @Override
    protected void addProbeInfo(IMultiblockController capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing sideHit) {
        IProbeInfo horizontalPane = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
        if (capability.isStructureFormed()) {
            horizontalPane.text(TextStyleClass.INFO + "{*gregtech.top.valid_structure*}");
            if(capability.isStructureObstructed()) {
                IProbeInfo horizontalPaneObstructed = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
                horizontalPaneObstructed.text(TextStyleClass.INFO + "{*gregtech.top.obstructed_structure*}");
            }

        }
        else
            horizontalPane.text(TextStyleClass.INFO + "{*gregtech.top.invalid_structure*}");
    }

    @Override
    public String getID() {
        return "gregtech:multiblock_controller_provider";
    }
}
