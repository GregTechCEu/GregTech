package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.common.metatileentities.electric.MetaTileEntityTransformer;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;

public class TransformerInfoProvider extends ElectricContainerInfoProvider {

    @Override
    public String getID() {
        return GTValues.MODID + ":transformer_info_provider";
    }

    @Override
    protected void addProbeInfo(@Nonnull IEnergyContainer capability, @Nonnull IProbeInfo probeInfo, EntityPlayer player, @Nonnull TileEntity tileEntity, @Nonnull IProbeHitData data) {
        if (tileEntity instanceof IGregTechTileEntity) {
            MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
            if (metaTileEntity instanceof MetaTileEntityTransformer) {
                StringBuilder input = new StringBuilder()
                        .append(GTValues.VNF[GTUtility.getTierByVoltage(capability.getInputVoltage())])
                        .append(TextFormatting.GREEN)
                        .append(" (")
                        .append(capability.getInputAmperage())
                        .append("A)");

                StringBuilder output = new StringBuilder()
                        .append(GTValues.VNF[GTUtility.getTierByVoltage(capability.getOutputVoltage())])
                        .append(TextFormatting.GREEN)
                        .append(" (")
                        .append(capability.getOutputAmperage())
                        .append("A)");

                // Step Up/Step Down line
                probeInfo.text(TextStyleClass.INFO + (((MetaTileEntityTransformer) metaTileEntity).isInverted() ? "{*gregtech.top.transform_up*} " : "{*gregtech.top.transform_down*} ") + input + " -> " + output);

                // Input/Output side line
                if (capability.inputsEnergy(data.getSideHit())) {
                    probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.transform_input*} " + input);
                } else if (capability.outputsEnergy(data.getSideHit())) {
                    probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.transform_output*} " + output);
                }
            }
        }
    }
}
