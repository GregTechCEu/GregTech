package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.ILaserContainer;
import gregtech.api.capability.ILaserRelay;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.NumberFormat;
import mcjty.theoneprobe.apiimpl.elements.ElementProgress;
import org.jetbrains.annotations.NotNull;

public class LaserContainerInfoProvider extends CapabilityInfoProvider<ILaserRelay> {

    @NotNull
    @Override
    protected Capability<ILaserRelay> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_LASER;
    }

    @Override
    protected boolean allowDisplaying(@NotNull ILaserRelay capability) {
        return capability instanceof ILaserContainer c && !c.isOneProbeHidden();
    }

    @Override
    protected void addProbeInfo(ILaserRelay cap, IProbeInfo probeInfo, EntityPlayer player,
                                TileEntity tileEntity, IProbeHitData data) {
        if (cap instanceof ILaserContainer capability) {
            long maxStorage = capability.getEnergyCapacity();
            long stored = capability.getEnergyStored();
            if (maxStorage == 0) return; // do not add empty max storage progress bar
            probeInfo.progress(stored, maxStorage, probeInfo.defaultProgressStyle()
                    .numberFormat(player.isSneaking() || stored < 10000 ? NumberFormat.FULL : NumberFormat.COMPACT)
                    .suffix(" / " + (player.isSneaking() || maxStorage < 10000 ? maxStorage + " EU" :
                            ElementProgress.format(maxStorage, NumberFormat.COMPACT, "EU")))
                    .filledColor(0xFFEEE600)
                    .alternateFilledColor(0xFFEEE600)
                    .borderColor(0xFF555555));
        }
    }

    @Override
    public String getID() {
        return GTValues.MODID + ":laser_container_provider";
    }
}
