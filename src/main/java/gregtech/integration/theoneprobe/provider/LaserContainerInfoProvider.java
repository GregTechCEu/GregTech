package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.ILaserContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import org.jetbrains.annotations.NotNull;

public class LaserContainerInfoProvider extends CapabilityInfoProvider<ILaserContainer> {

    @NotNull
    @Override
    protected Capability<ILaserContainer> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_LASER;
    }

    @Override
    protected boolean allowDisplaying(@NotNull ILaserContainer capability) {
        return !capability.isOneProbeHidden();
    }

    @Override
    protected void addProbeInfo(ILaserContainer capability, IProbeInfo probeInfo, EntityPlayer player,
                                TileEntity tileEntity, IProbeHitData data) {
        long maxStorage = capability.getEnergyCapacity();
        if (maxStorage == 0) return; // do not add empty max storage progress bar
        probeInfo.progress(capability.getEnergyStored(), maxStorage, probeInfo.defaultProgressStyle()
                .suffix(" / " + maxStorage + " EU")
                .filledColor(0xFFEEE600)
                .alternateFilledColor(0xFFEEE600)
                .borderColor(0xFF555555));
    }

    @Override
    public String getID() {
        return GTValues.MODID + ":laser_container_provider";
    }
}
