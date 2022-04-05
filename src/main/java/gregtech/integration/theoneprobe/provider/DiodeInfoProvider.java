package gregtech.integration.theoneprobe.provider;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.common.metatileentities.electric.MetaTileEntityDiode;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class DiodeInfoProvider extends ElectricContainerInfoProvider {

    @Override
    public String getID() {
        return "gregtech:diode_info_provider";
    }

    @Override
    protected void addProbeInfo(IEnergyContainer capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing sideHit) {
        if (tileEntity instanceof IGregTechTileEntity) {
            MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
            if (metaTileEntity instanceof MetaTileEntityDiode) {
                long inputAmperage = capability.getInputAmperage();
                long outputAmperage = capability.getOutputAmperage();
                IProbeInfo horizontalPane = probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
                String transformInfo;
                if (capability.inputsEnergy(sideHit)) {
                    transformInfo = "{*gregtech.top.transform_input*} " + inputAmperage + "A";
                    horizontalPane.text(TextStyleClass.INFO + transformInfo);
                } else if (capability.outputsEnergy(sideHit)) {
                    transformInfo = "{*gregtech.top.transform_output*} " + outputAmperage + "A";
                    horizontalPane.text(TextStyleClass.INFO + transformInfo);
                }
            }
        }
    }
}
