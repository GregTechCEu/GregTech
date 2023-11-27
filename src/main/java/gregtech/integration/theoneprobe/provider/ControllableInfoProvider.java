package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import org.jetbrains.annotations.NotNull;

public class ControllableInfoProvider extends CapabilityInfoProvider<IControllable> {

    @NotNull
    @Override
    protected Capability<IControllable> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE;
    }

    @Override
    public String getID() {
        return GTValues.MODID + ":controllable_provider";
    }

    @Override
    protected void addProbeInfo(@NotNull IControllable capability, @NotNull IProbeInfo probeInfo, EntityPlayer player,
                                @NotNull TileEntity tileEntity, @NotNull IProbeHitData data) {
        if (!capability.isWorkingEnabled())
            probeInfo.text(TextStyleClass.WARNING + "{*gregtech.top.working_disabled*}");
    }
}
