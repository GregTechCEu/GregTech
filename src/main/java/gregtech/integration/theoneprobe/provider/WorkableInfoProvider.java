package gregtech.integration.theoneprobe.provider;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class WorkableInfoProvider extends CapabilityInfoProvider<IWorkable> {

    @Override
    protected Capability<IWorkable> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_WORKABLE;
    }

    @Override
    public String getID() {
        return "gregtech:workable_provider";
    }

    @Override
    protected void addProbeInfo(IWorkable capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing sideHit) {
        int currentProgress = capability.getProgress();
        int maxProgress = capability.getMaxProgress();
        String suffix;

        if (maxProgress < 20) {
            currentProgress = capability.getProgress();
            maxProgress = capability.getMaxProgress();
            suffix = " t / " + maxProgress + " t";
        } else {
            currentProgress = (int) Math.round(currentProgress / 20.0);
            maxProgress = (int) Math.round(maxProgress / 20.0);
            suffix = " s / " + maxProgress + " s";
        }

        if (maxProgress > 0) {
            IProbeInfo horizontalPane = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
            horizontalPane.text(TextStyleClass.INFO + "{*gregtech.top.progress*} ");
            horizontalPane.progress(currentProgress, maxProgress, probeInfo.defaultProgressStyle()
                    .suffix(suffix)
                    .borderColor(0x00000000)
                    .backgroundColor(0x00000000)
                    .filledColor(0xFF000099)
                    .alternateFilledColor(0xFF000077));
        }
    }
}
