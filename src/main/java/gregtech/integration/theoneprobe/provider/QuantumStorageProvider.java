package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.IQuantumController;
import gregtech.api.capability.IQuantumStorage;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import org.jetbrains.annotations.NotNull;

public class QuantumStorageProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return ":quantum_storage_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world,
                             IBlockState blockState, IProbeHitData data) {
        if (blockState.getBlock().hasTileEntity(blockState) &&
                world.getTileEntity(data.getPos()) instanceof IGregTechTileEntity gtte) {
            if (gtte.getMetaTileEntity() instanceof IQuantumController controller) {
                String eutText = configureEnergyUsage(controller.getEnergyUsage() / 10);
                if (controller.getCount(IQuantumStorage.Type.ENERGY) == 0) {
                    probeInfo.text("{*gregtech.top.quantum_controller.no_hatches*}");
                } else if (!controller.isPowered()) {
                    probeInfo.text("{*gregtech.top.quantum_controller.no_power*}");
                    probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.energy_required*} " + eutText);
                } else {
                    probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.energy_consumption*} " + eutText);
                }
            } else if (gtte.getMetaTileEntity() instanceof IQuantumStorage<?>storage &&
                    (storage.getType() == IQuantumStorage.Type.ITEM ||
                            storage.getType() == IQuantumStorage.Type.FLUID)) {
                                probeInfo.text(getConnectionStatus(storage));
                            }
        }
    }

    private static @NotNull String getConnectionStatus(IQuantumStorage<?> storage) {
        var qcontroller = storage.getQuantumController();
        String status = "gregtech.top.quantum_status.disconnected";
        if (qcontroller != null) {
            status = qcontroller.isPowered() ?
                    "gregtech.top.quantum_status.powered" :
                    "gregtech.top.quantum_status.connected";
        }
        return TextStyleClass.INFO + "{*gregtech.top.quantum_status.label*} " +
                "{*" + status + "*}";
    }

    public String configureEnergyUsage(long EUs) {
        return TextFormatting.RED.toString() + EUs + TextStyleClass.INFO + " EU/t" + TextFormatting.GREEN +
                " (" + GTValues.VNF[GTUtility.getTierByVoltage(EUs)] + TextFormatting.GREEN + ")";
    }
}
