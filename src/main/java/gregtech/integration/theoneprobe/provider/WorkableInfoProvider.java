package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;

public class WorkableInfoProvider extends CapabilityInfoProvider<IWorkable> {

    @Override
    public String getID() {
        return GTValues.MODID + ":workable_provider";
    }

    @Nonnull
    @Override
    protected Capability<IWorkable> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_WORKABLE;
    }

    @Override
    protected void addProbeInfo(@Nonnull IWorkable capability, @Nonnull IProbeInfo probeInfo, @Nonnull EntityPlayer player, @Nonnull TileEntity tileEntity, @Nonnull IProbeHitData data) {
        if (!capability.isActive()) return;

        int currentProgress = capability.getProgress();
        int maxProgress = capability.getMaxProgress();
        String text;

        if (maxProgress < 20) {
            text = " / " + maxProgress + " t";
        } else {
            currentProgress = Math.round(currentProgress / 20.0F);
            maxProgress = Math.round(maxProgress / 20.0F);
            text = " / " + maxProgress + " s";
        }

        if (maxProgress > 0) {
            int color = capability.isWorkingEnabled() ? 0xFF4CBB17 : 0xFFBB1C28;
            probeInfo.progress(currentProgress, maxProgress, probeInfo.defaultProgressStyle()
                    .suffix(text)
                    .filledColor(color)
                    .alternateFilledColor(color)
                    .borderColor(0xFF555555));
        }
    }
}
