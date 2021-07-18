package gregtech.integration.theoneprobe.provider;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IFuelInfo;
import gregtech.api.capability.IFuelable;
import gregtech.api.capability.impl.ItemFuelInfo;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import java.util.Collection;

public class FuelableInfoProvider extends CapabilityInfoProvider<IFuelable> {

    @Override
    protected Capability<IFuelable> getCapability() {
        return GregtechCapabilities.CAPABILITY_FUELABLE;
    }

    @Override
    public String getID() {
        return "gregtech:fuelable_provider";
    }

    @Override
    protected boolean allowDisplaying(IFuelable capability) {
        return !capability.isOneProbeHidden();
    }

    @Override
    protected void addProbeInfo(IFuelable capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing sideHit) {
        Collection<IFuelInfo> fuels = capability.getFuels();
        if (fuels == null || fuels.isEmpty()) {
            probeInfo.text(TextStyleClass.WARNING + "{*gregtech.top.fuel_none*}");
            return;
        }
        for (IFuelInfo fuelInfo : fuels) {
            final String fuelName = fuelInfo.getFuelName();
            final int fuelRemaining = fuelInfo.getFuelRemaining();
            final int fuelCapacity = fuelInfo.getFuelCapacity();
            final int fuelMinConsumed = fuelInfo.getFuelMinConsumed();
            final long burnTime = fuelInfo.getFuelBurnTimeLong() / 20;

            IProbeInfo horizontalPane = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
            if (fuelInfo instanceof ItemFuelInfo) {
                horizontalPane.text(TextStyleClass.INFO + "{*gregtech.top.fuel_name*} ").itemLabel(((ItemFuelInfo) fuelInfo).getItemStack());
            } else {
                horizontalPane.text(TextStyleClass.INFO + "{*gregtech.top.fuel_name*} {*" + fuelName + "*}");
            }

            horizontalPane = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
            horizontalPane.progress(fuelRemaining, fuelCapacity, probeInfo.defaultProgressStyle()
                .suffix("/" + fuelCapacity + " ")
                .borderColor(0x00000000)
                .backgroundColor(0x00000000)
                .filledColor(0xFFFFE000)
                .alternateFilledColor(0xFFEED000));
            if (fuelRemaining < fuelMinConsumed)
                horizontalPane.text("{*gregtech.top.fuel_min_consume*} " + fuelMinConsumed);
            else
                horizontalPane.text("{*gregtech.top.fuel_burn*} " + burnTime + " {*gregtech.top.fuel_time*}");
        }
    }

}
