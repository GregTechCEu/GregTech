package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;

public class ControllableInfoProvider extends CapabilityInfoProvider<IControllable> {

    @Nonnull
    @Override
    protected Capability<IControllable> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE;
    }

    @Override
    public String getID() {
        return GTValues.MODID + ":controllable_provider";
    }

    @Override
    protected void addProbeInfo(@Nonnull IControllable capability, @Nonnull IProbeInfo probeInfo, EntityPlayer player, @Nonnull TileEntity tileEntity, @Nonnull IProbeHitData data) {
        if (!capability.isWorkingEnabled()) probeInfo.text(TextStyleClass.WARNING + "{*gregtech.top.working_disabled*}");
    }
}
