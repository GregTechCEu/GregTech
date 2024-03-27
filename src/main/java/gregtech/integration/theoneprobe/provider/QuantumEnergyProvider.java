package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.IQuantumController;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumEnergyAcceptor;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;

public class QuantumEnergyProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return ":quantum_energy_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world,
                             IBlockState blockState, IProbeHitData data) {
        if (blockState.getBlock().hasTileEntity(blockState) &&
                world.getTileEntity(data.getPos()) instanceof IGregTechTileEntity gtte) {
            if (gtte.getMetaTileEntity() instanceof IQuantumController controller) {
                configureEnergyUsage(controller.getEnergyUsage() / 10, probeInfo);
            } else if (gtte.getMetaTileEntity() instanceof MetaTileEntityQuantumEnergyAcceptor energyAcceptor) {

                // todo show something when disconnected?
                if (energyAcceptor.isConnected()) {
                    configureEnergyUsage(energyAcceptor.getController().getEnergyUsage() / 10, probeInfo);
                }
            }
        }
    }

    public void configureEnergyUsage(long EUs, IProbeInfo probeInfo) {
        String text = TextFormatting.RED.toString() + EUs + TextStyleClass.INFO + " EU/t" + TextFormatting.GREEN +
                " (" + GTValues.VNF[GTUtility.getTierByVoltage(EUs)] + TextFormatting.GREEN + ")";
        probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.energy_consumption*} " + text);
    }
}
