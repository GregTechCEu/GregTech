package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.util.GTUtility;
import gregtech.common.covers.*;
import gregtech.common.covers.filter.fluid.FluidFilter;
import gregtech.common.covers.filter.fluid.FluidFilterHolder;
import gregtech.common.covers.filter.item.ItemFilter;
import gregtech.common.covers.filter.item.ItemFilterHolder;
import gregtech.common.covers.filter.item.OreDictFilter;
import gregtech.common.covers.filter.item.SmartFilter;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CoverProvider extends CapabilityInfoProvider<ICoverable> {

    @Nonnull
    @Override
    protected Capability<ICoverable> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_COVERABLE;
    }

    @Override
    public String getID() {
        return GTValues.MODID + ":coverable_provider";
    }

    @Override
    protected void addProbeInfo(@Nonnull ICoverable capability, @Nonnull IProbeInfo probeInfo, @Nonnull EntityPlayer player, @Nonnull TileEntity tileEntity, @Nonnull IProbeHitData data) {
        CoverBehavior coverBehavior = capability.getCoverAtSide(data.getSideHit());
        if (coverBehavior instanceof CoverConveyor) {
            conveyorInfo(probeInfo, (CoverConveyor) coverBehavior);
        } else if (coverBehavior instanceof CoverPump) {
            pumpInfo(probeInfo, (CoverPump) coverBehavior);
        } else if (coverBehavior instanceof CoverItemFilter) {
            itemFilterInfo(probeInfo, (CoverItemFilter) coverBehavior);
        } else if (coverBehavior instanceof CoverFluidFilter) {
            fluidFilterInfo(probeInfo, (CoverFluidFilter) coverBehavior);
        } else if (coverBehavior instanceof CoverEnderFluidLink) {
            enderFluidLinkInfo(probeInfo, (CoverEnderFluidLink) coverBehavior);
        }
    }

    /**
     * Displays text for {@link CoverConveyor} related covers
     *
     * @param probeInfo the info to add the text to
     * @param conveyor  the conveyor to get data from
     */
    private static void conveyorInfo(@Nonnull IProbeInfo probeInfo, @Nonnull CoverConveyor conveyor) {
        String rateUnit = " {*cover.conveyor.transfer_rate*}";

        if (conveyor instanceof CoverItemVoiding) {
            itemVoidingInfo(probeInfo, (CoverItemVoiding) conveyor);
        } else if (!(conveyor instanceof CoverRoboticArm) || ((CoverRoboticArm) conveyor).getTransferMode() == TransferMode.TRANSFER_ANY) {
            // only display the regular rate if the cover does not have a specialized rate
            transferRateText(probeInfo, conveyor.getConveyorMode(), rateUnit, conveyor.getTransferRate());
        }

        ItemFilterHolder filterHolder = conveyor.getFilterHolder();
        if (conveyor instanceof CoverRoboticArm) {
            CoverRoboticArm roboticArm = (CoverRoboticArm) conveyor;
            transferModeText(probeInfo, roboticArm.getTransferMode(), rateUnit, filterHolder.getTransferStackSize(), filterHolder.hasFilter());
        }
        itemFilterText(probeInfo, filterHolder.getCurrentFilter());
    }

    /**
     * Displays info for {@link CoverItemVoiding} related covers
     *
     * @param probeInfo the info to add the text to
     * @param voiding   the voiding cover to get data from
     */
    private static void itemVoidingInfo(@Nonnull IProbeInfo probeInfo, @Nonnull CoverItemVoiding voiding) {
        String unit = " {*gregtech.top.unit.items*}";

        ItemFilterHolder filterHolder = voiding.getFilterHolder();
        if (voiding instanceof CoverItemVoidingAdvanced) {
            CoverItemVoidingAdvanced advanced = (CoverItemVoidingAdvanced) voiding;
            VoidingMode mode = advanced.getVoidingMode();
            voidingText(probeInfo, mode, unit, filterHolder.getTransferStackSize(), filterHolder.hasFilter());
        }
    }

    /**
     * Displays text for {@link CoverPump} related covers
     *
     * @param probeInfo the info to add the text to
     * @param pump      the pump to get data from
     */
    private static void pumpInfo(@Nonnull IProbeInfo probeInfo, @Nonnull CoverPump pump) {
        String rateUnit = IProbeInfo.STARTLOC + pump.getBucketMode().getName() + IProbeInfo.ENDLOC;

        if (pump instanceof CoverFluidVoiding) {
            fluidVoidingInfo(probeInfo, (CoverFluidVoiding) pump);
        } else if (!(pump instanceof CoverFluidRegulator) || ((CoverFluidRegulator) pump).getTransferMode() == TransferMode.TRANSFER_ANY) {
            // do not display the regular rate if the cover has a specialized rate
            transferRateText(probeInfo, pump.getPumpMode(), " " + rateUnit, pump.getBucketMode() == CoverPump.BucketMode.BUCKET ? pump.getTransferRate() / 1000 : pump.getTransferRate());
        }

        FluidFilterHolder filterHolder = pump.getFilterHolder();
        if (pump instanceof CoverFluidRegulator) {
            CoverFluidRegulator regulator = (CoverFluidRegulator) pump;
            transferModeText(probeInfo, regulator.getTransferMode(), rateUnit, regulator.getTransferAmount(), filterHolder.hasFilter());
        }
        fluidFilterText(probeInfo, filterHolder.getCurrentFilter());
    }

    /**
     * Displays info for {@link CoverFluidVoiding} related covers
     *
     * @param probeInfo the info to add the text to
     * @param voiding   the voiding cover to get data from
     */
    private static void fluidVoidingInfo(@Nonnull IProbeInfo probeInfo, @Nonnull CoverFluidVoiding voiding) {
        String unit = voiding.getBucketMode() == CoverPump.BucketMode.BUCKET ? " {*gregtech.top.unit.fluid_buckets*}" : " {*gregtech.top.unit.fluid_milibuckets*}";

        if (voiding instanceof CoverFluidVoidingAdvanced) {
            CoverFluidVoidingAdvanced advanced = (CoverFluidVoidingAdvanced) voiding;
            VoidingMode mode = advanced.getVoidingMode();
            // do not display amount in overflow when a filter is present
            voidingText(probeInfo, mode, unit, voiding.getBucketMode() == CoverPump.BucketMode.BUCKET ? advanced.getTransferAmount() / 1000 : advanced.getTransferAmount(), voiding.getFilterHolder().hasFilter());
        }
    }

    /**
     * Displays text for {@link CoverItemFilter} related covers
     *
     * @param probeInfo  the info to add the text to
     * @param itemFilter the filter to get data from
     */
    private static void itemFilterInfo(@Nonnull IProbeInfo probeInfo, @Nonnull CoverItemFilter itemFilter) {
        filterModeText(probeInfo, itemFilter.getFilterMode());
        itemFilterText(probeInfo, itemFilter.getItemFilter());
    }

    /**
     * Displays text for {@link CoverFluidFilter} related covers
     *
     * @param probeInfo   the info to add the text to
     * @param fluidFilter the filter to get data from
     */
    private static void fluidFilterInfo(@Nonnull IProbeInfo probeInfo, @Nonnull CoverFluidFilter fluidFilter) {
        filterModeText(probeInfo, fluidFilter.getFilterMode());
        fluidFilterText(probeInfo, fluidFilter.getFluidFilter());
    }

    /**
     * Displays text for {@link CoverEnderFluidLink} related covers
     *
     * @param probeInfo      the info to add the text to
     * @param enderFluidLink the ender fluid link cover to get data from
     */
    private static void enderFluidLinkInfo(@Nonnull IProbeInfo probeInfo, @Nonnull CoverEnderFluidLink enderFluidLink) {
        transferRateText(probeInfo, enderFluidLink.getPumpMode(), " {*cover.bucket.mode.milli_bucket*}", enderFluidLink.isIOEnabled() ? CoverEnderFluidLink.TRANSFER_RATE : 0);
        fluidFilterText(probeInfo, enderFluidLink.getFilterHolder().getCurrentFilter());

        if (!enderFluidLink.getColorStr().isEmpty()) {
            probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.link_cover.color*} " + enderFluidLink.getColorStr());
        }
    }


    /**
     * Displays text for {@link IIOMode} covers
     *
     * @param probeInfo the info to add the text to
     * @param mode      the transfer mode of the cover
     * @param rateUnit  the unit of what is transferred
     * @param rate      the transfer rate of the mode
     */
    private static void transferRateText(@Nonnull IProbeInfo probeInfo, @Nonnull IIOMode mode, @Nonnull String rateUnit, int rate) {
        String modeText = mode.isImport() ? "{*gregtech.top.mode.import*} " : "{*gregtech.top.mode.export*} ";
        probeInfo.text(TextStyleClass.OK + modeText + TextStyleClass.LABEL + GTUtility.formatNumbers(rate) + rateUnit);
    }

    /**
     * Displays text for {@link TransferMode} covers
     *
     * @param probeInfo the info to add the text to
     * @param mode      the transfer mode of the cover
     * @param rateUnit  the unit of what is transferred
     * @param rate      the transfer rate of the mode
     * @param hasFilter whether the cover has a filter installed
     */
    private static void transferModeText(@Nonnull IProbeInfo probeInfo, @Nonnull TransferMode mode, @Nonnull String rateUnit, int rate, boolean hasFilter) {
        String text = TextStyleClass.OK + IProbeInfo.STARTLOC + mode.getName() + IProbeInfo.ENDLOC;
        if (!hasFilter && mode != TransferMode.TRANSFER_ANY) text += TextStyleClass.LABEL + " " + rate + rateUnit;
        probeInfo.text(text);
    }

    /**
     * Displays text for {@link VoidingMode} covers
     *
     * @param probeInfo the info to add the text to
     * @param mode      the transfer mode of the cover
     * @param unit      the unit of what is transferred
     * @param amount    the transfer rate of the mode
     * @param hasFilter whether the cover has a filter in it or not
     */
    private static void voidingText(@Nonnull IProbeInfo probeInfo, @Nonnull VoidingMode mode, @Nonnull String unit, int amount, boolean hasFilter) {
        String text = TextFormatting.RED + IProbeInfo.STARTLOC + mode.getName() + IProbeInfo.ENDLOC;
        if (mode != VoidingMode.VOID_ANY && !hasFilter) text += " " + amount + unit;
        probeInfo.text(text);
    }

    /**
     * Displays text for {@link IFilterMode} covers
     *
     * @param probeInfo the info to add the text to
     * @param mode      the filter mode of the cover
     */
    private static void filterModeText(@Nonnull IProbeInfo probeInfo, @Nonnull IFilterMode mode) {
        probeInfo.text(TextStyleClass.WARNING + IProbeInfo.STARTLOC + mode.getName() + IProbeInfo.ENDLOC);
    }

    /**
     * Displays text for {@link ItemFilter} covers
     *
     * @param probeInfo the info to add the text to
     * @param filter    the filter to display info from
     */
    private static void itemFilterText(@Nonnull IProbeInfo probeInfo, @Nullable ItemFilter filter) {
        String label = TextStyleClass.INFO + "{*gregtech.top.filter.label*} ";
        if (filter instanceof OreDictFilter) {
            String expression = ((OreDictFilter) filter).getOreDictFilterExpression();
            if (!expression.isEmpty()) probeInfo.text(label + expression);
        } else if (filter instanceof SmartFilter) {
            probeInfo.text(label + IProbeInfo.STARTLOC + ((SmartFilter) filter).getFilteringMode().getName() + IProbeInfo.ENDLOC);
        }
    }

    /**
     * Displays text for {@link FluidFilter} covers
     *
     * @param probeInfo the info to add the text to
     * @param filter    the filter to display info from
     */
    private static void fluidFilterText(@Nonnull IProbeInfo probeInfo, @Nullable FluidFilter filter) {
        // TODO If more unique fluid filtration is added, providers for it go here
    }
}
