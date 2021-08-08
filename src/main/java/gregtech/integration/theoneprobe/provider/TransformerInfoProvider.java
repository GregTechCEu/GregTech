package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.util.GTUtility;
import gregtech.common.metatileentities.electric.MetaTileEntityTransformer;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class TransformerInfoProvider extends ElectricContainerInfoProvider {

    @Override
    public String getID() {
        return "gregtech:transformer_info_provider";
    }

    @Override
    protected void addProbeInfo(IEnergyContainer capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing sideHit) {
        if (tileEntity instanceof MetaTileEntityHolder) {
            MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
            if (metaTileEntity instanceof MetaTileEntityTransformer) {
                MetaTileEntityTransformer mteTransformer = (MetaTileEntityTransformer) metaTileEntity;
                String inputVoltageN = GTValues.VN[GTUtility.getTierByVoltage(capability.getInputVoltage())];
                String outputVoltageN = GTValues.VN[GTUtility.getTierByVoltage(capability.getOutputVoltage())];
                long inputAmperage = capability.getInputAmperage();
                long outputAmperage = capability.getOutputAmperage();
                IProbeInfo horizontalPane = probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
                String transformInfo;

                // Step Up/Step Down line
                if (mteTransformer.isInverted()) {
                    transformInfo = "{*gregtech.top.transform_up*} ";
                } else {
                    transformInfo = "{*gregtech.top.transform_down*} ";
                }
                transformInfo += inputVoltageN + " (" + inputAmperage + "A) -> "
                        + outputVoltageN + " (" + outputAmperage + "A)";
                horizontalPane.text(TextStyleClass.INFO + transformInfo);

                // Input/Output side line
                horizontalPane = probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
                if (capability.inputsEnergy(sideHit)) {
                    transformInfo = "{*gregtech.top.transform_input*} "
                            + inputVoltageN + " (" + inputAmperage + "A)";
                    horizontalPane.text(TextStyleClass.INFO + transformInfo);

                } else if (capability.outputsEnergy(sideHit)) {
                    transformInfo = "{*gregtech.top.transform_output*} "
                            + outputVoltageN + " (" + outputAmperage + "A)";
                    horizontalPane.text(TextStyleClass.INFO + transformInfo);
                }
            }
        }
    }
}
