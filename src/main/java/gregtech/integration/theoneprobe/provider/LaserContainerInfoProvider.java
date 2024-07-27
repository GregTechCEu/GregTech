package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.ILaserRelay;
import gregtech.api.util.TextFormattingUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.NumberFormat;
import org.jetbrains.annotations.NotNull;

public class LaserContainerInfoProvider extends CapabilityInfoProvider<ILaserRelay> {

    @NotNull
    @Override
    protected Capability<ILaserRelay> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_LASER;
    }

    @Override
    protected boolean allowDisplaying(@NotNull ILaserRelay capability) {
        return !capability.isOneProbeHidden();
    }

    @Override
    protected void addProbeInfo(ILaserRelay capability, IProbeInfo probeInfo, EntityPlayer player,
                                TileEntity tileEntity, IProbeHitData data) {
        long maxStorage = capability.getEnergyCapacity();
        if (maxStorage == 0) return; // do not add empty max storage progress bar
        probeInfo.progress(capability.getEnergyStored(), maxStorage, probeInfo.defaultProgressStyle()
                .suffix(" / " + TextFormattingUtil.formatNumbers(maxStorage) + " EU")
                .filledColor(0xFFEEE600)
                .alternateFilledColor(0xFFEEE600)
                .borderColor(0xFF555555).numberFormat(NumberFormat.COMMAS));
    }

    @Override
    public String getID() {
        return GTValues.MODID + ":laser_container_provider";
    }
}
