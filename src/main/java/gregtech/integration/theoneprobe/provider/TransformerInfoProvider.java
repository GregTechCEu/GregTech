package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.common.metatileentities.electric.MetaTileEntityTransformer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import org.jetbrains.annotations.NotNull;

public class TransformerInfoProvider extends ElectricContainerInfoProvider {

    @Override
    public String getID() {
        return GTValues.MODID + ":transformer_info_provider";
    }

    @Override
    protected void addProbeInfo(@NotNull IEnergyContainer capability, @NotNull IProbeInfo probeInfo,
                                EntityPlayer player, @NotNull TileEntity tileEntity, @NotNull IProbeHitData data) {
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
                probeInfo.text(TextStyleClass.INFO +
                        (((MetaTileEntityTransformer) metaTileEntity).isInverted() ?
                                TextFormatting.RED + "{*gregtech.top.transform_up*} " + TextFormatting.RESET :
                                TextFormatting.GREEN + "{*gregtech.top.transform_down*} " + TextFormatting.RESET) +
                        input + " -> " + output);

                // Input/Output side line
                if (capability.inputsEnergy(data.getSideHit())) {
                    probeInfo.text(TextStyleClass.INFO + TextFormatting.GOLD.toString() +
                            "{*gregtech.top.transform_input*} " + TextFormatting.RESET + input);
                } else if (capability.outputsEnergy(data.getSideHit())) {
                    probeInfo.text(TextStyleClass.INFO + TextFormatting.BLUE.toString() +
                            "{*gregtech.top.transform_output*} " + TextFormatting.RESET + output);
                }
            }
        }
    }
}
