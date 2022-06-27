package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IFuelInfo;
import gregtech.api.capability.IFuelable;
import gregtech.api.capability.impl.ItemFuelInfo;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StringUtils;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import java.util.Collection;

public class FuelableInfoProvider extends CapabilityInfoProvider<IFuelable> {

    @Override
    public String getID() {
        return GTValues.MODID + ":fuelable_provider";
    }

    @Nonnull
    @Override
    protected Capability<IFuelable> getCapability() {
        return GregtechCapabilities.CAPABILITY_FUELABLE;
    }

    @Override
    protected boolean allowDisplaying(@Nonnull IFuelable capability) {
        return !capability.isOneProbeHidden();
    }

    @Override
    protected void addProbeInfo(@Nonnull IFuelable capability, IProbeInfo probeInfo, EntityPlayer player, TileEntity tileEntity, IProbeHitData data) {
        Collection<IFuelInfo> fuels = capability.getFuels();
        if (fuels == null || fuels.isEmpty()) {
            probeInfo.text(TextStyleClass.WARNING + "{*gregtech.top.fuel_none*}");
            return;
        }
        for (IFuelInfo fuelInfo : fuels) {
            int fuelRemaining = fuelInfo.getFuelRemaining();
            IProbeInfo horizontal = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
            if (fuelInfo instanceof ItemFuelInfo) {
                horizontal.item(((ItemFuelInfo) fuelInfo).getItemStack()).text(TextStyleClass.INFO + IProbeInfo.STARTLOC + ((ItemFuelInfo) fuelInfo).getItemStack().getTranslationKey() + ".name" + IProbeInfo.ENDLOC);
            } else {
                horizontal.text(TextStyleClass.INFO + "{*" + fuelInfo.getFuelName() + "*}");
            }
            horizontal.text(TextStyleClass.LABEL + " " + fuelRemaining + " / " + fuelInfo.getFuelCapacity());

            int fuelMinConsumed = fuelInfo.getFuelMinConsumed();
            if (fuelRemaining < fuelMinConsumed) probeInfo.text(TextStyleClass.INFOIMP + "{*gregtech.top.fuel_min_consume*} " + fuelMinConsumed);
            else probeInfo.text(TextStyleClass.INFO + StringUtils.ticksToElapsedTime((int) fuelInfo.getFuelBurnTimeLong()));
        }
    }
}
