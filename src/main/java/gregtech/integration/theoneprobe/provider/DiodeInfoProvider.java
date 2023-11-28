package gregtech.integration.theoneprobe.provider;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.common.metatileentities.electric.MetaTileEntityDiode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import org.jetbrains.annotations.NotNull;

public class DiodeInfoProvider extends ElectricContainerInfoProvider {

    @Override
    public String getID() {
        return "gregtech:diode_info_provider";
    }

    @Override
    protected void addProbeInfo(@NotNull IEnergyContainer capability, @NotNull IProbeInfo probeInfo,
                                EntityPlayer player, @NotNull TileEntity tileEntity, @NotNull IProbeHitData data) {
        if (tileEntity instanceof IGregTechTileEntity) {
            MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
            if (metaTileEntity instanceof MetaTileEntityDiode) {
                if (capability.inputsEnergy(data.getSideHit())) {
                    probeInfo.text(
                            TextStyleClass.INFO + TextFormatting.GOLD.toString() + "{*gregtech.top.transform_input*} " +
                                    TextFormatting.RESET + capability.getInputAmperage() + " A");
                } else if (capability.outputsEnergy(data.getSideHit())) {
                    probeInfo.text(TextStyleClass.INFO + TextFormatting.BLUE.toString() +
                            "{*gregtech.top.transform_output*} " + TextFormatting.RESET +
                            capability.getOutputAmperage() + " A");
                }
            }
        }
    }
}
