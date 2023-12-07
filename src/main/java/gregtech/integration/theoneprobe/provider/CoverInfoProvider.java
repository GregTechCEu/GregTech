package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.Cover;
import gregtech.api.cover.CoverHolder;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.covers.*;
import gregtech.common.covers.filter.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CoverInfoProvider extends CapabilityInfoProvider<CoverHolder> {

    @NotNull
    @Override
    protected Capability<CoverHolder> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_COVER_HOLDER;
    }

    @Override
    public String getID() {
        return GTValues.MODID + ":coverable_provider";
    }

    @Override
    protected void addProbeInfo(@NotNull CoverHolder capability, @NotNull IProbeInfo probeInfo,
                                @NotNull EntityPlayer player, @NotNull TileEntity tileEntity,
                                @NotNull IProbeHitData data) {
        Cover cover = capability.getCoverAtSide(data.getSideHit());
        if (cover instanceof CoverConveyor conveyor) {
            conveyorInfo(probeInfo, conveyor);
        } else if (cover instanceof CoverPump coverPump) {
            pumpInfo(probeInfo, coverPump);
        } else if (cover instanceof CoverItemFilter itemFilter) {
            itemFilterInfo(probeInfo, itemFilter);
        } else if (cover instanceof CoverFluidFilter fluidFilter) {
            fluidFilterInfo(probeInfo, fluidFilter);
        } else if (cover instanceof CoverEnderFluidLink enderFluidLink) {
            enderFluidLinkInfo(probeInfo, enderFluidLink);
        }
    }

    /**
     * Displays text for {@link CoverConveyor} related covers
     *
     * @param probeInfo the info to add the text to
     * @param conveyor  the conveyor to get data from
     */
    private static void conveyorInfo(@NotNull IProbeInfo probeInfo, @NotNull CoverConveyor conveyor) {
        String rateUnit = " {*cover.conveyor.transfer_rate*}";

        if (conveyor instanceof CoverItemVoiding) {
            itemVoidingInfo(probeInfo, (CoverItemVoiding) conveyor);
        } else if (!(conveyor instanceof CoverRoboticArm) ||
                ((CoverRoboticArm) conveyor).getTransferMode() == TransferMode.TRANSFER_ANY) {
                    // only display the regular rate if the cover does not have a specialized rate
                    transferRateText(probeInfo, conveyor.getConveyorMode(), rateUnit, conveyor.getTransferRate());
                }

        ItemFilterContainer filter = conveyor.getItemFilterContainer();
        if (conveyor instanceof CoverRoboticArm roboticArm) {
            transferModeText(probeInfo, roboticArm.getTransferMode(), rateUnit, filter.getTransferStackSize(),
                    filter.getFilterWrapper().getItemFilter() != null);
        }
        itemFilterText(probeInfo, filter.getFilterWrapper().getItemFilter());
    }

    /**
     * Displays info for {@link CoverItemVoiding} related covers
     *
     * @param probeInfo the info to add the text to
     * @param voiding   the voiding cover to get data from
     */
    private static void itemVoidingInfo(@NotNull IProbeInfo probeInfo, @NotNull CoverItemVoiding voiding) {
        String unit = " {*gregtech.top.unit.items*}";

        ItemFilterContainer container = voiding.getItemFilterContainer();
        if (voiding instanceof CoverItemVoidingAdvanced advanced) {
            VoidingMode mode = advanced.getVoidingMode();
            voidingText(probeInfo, mode, unit, container.getTransferStackSize(),
                    container.getFilterWrapper().getItemFilter() != null);
        }
    }

    /**
     * Displays text for {@link CoverPump} related covers
     *
     * @param probeInfo the info to add the text to
     * @param pump      the pump to get data from
     */
    private static void pumpInfo(@NotNull IProbeInfo probeInfo, @NotNull CoverPump pump) {
        String rateUnit = IProbeInfo.STARTLOC + pump.getBucketMode().getName() + IProbeInfo.ENDLOC;

        if (pump instanceof CoverFluidVoiding) {
            fluidVoidingInfo(probeInfo, (CoverFluidVoiding) pump);
        } else if (!(pump instanceof CoverFluidRegulator) ||
                ((CoverFluidRegulator) pump).getTransferMode() == TransferMode.TRANSFER_ANY) {
                    // do not display the regular rate if the cover has a specialized rate
                    transferRateText(probeInfo, pump.getPumpMode(), " " + rateUnit,
                            pump.getBucketMode() == CoverPump.BucketMode.BUCKET ? pump.getTransferRate() / 1000 :
                                    pump.getTransferRate());
                }

        FluidFilterContainer filter = pump.getFluidFilterContainer();
        if (pump instanceof CoverFluidRegulator regulator) {
            transferModeText(probeInfo, regulator.getTransferMode(), rateUnit, regulator.getTransferAmount(),
                    filter.getFilterWrapper().getFluidFilter() != null);
        }
        fluidFilterText(probeInfo, filter.getFilterWrapper().getFluidFilter());
    }

    /**
     * Displays info for {@link CoverFluidVoiding} related covers
     *
     * @param probeInfo the info to add the text to
     * @param voiding   the voiding cover to get data from
     */
    private static void fluidVoidingInfo(@NotNull IProbeInfo probeInfo, @NotNull CoverFluidVoiding voiding) {
        String unit = voiding.getBucketMode() == CoverPump.BucketMode.BUCKET ? " {*gregtech.top.unit.fluid_buckets*}" :
                " {*gregtech.top.unit.fluid_milibuckets*}";

        if (voiding instanceof CoverFluidVoidingAdvanced advanced) {
            VoidingMode mode = advanced.getVoidingMode();
            // do not display amount in overflow when a filter is present
            voidingText(probeInfo, mode, unit,
                    voiding.getBucketMode() == CoverPump.BucketMode.BUCKET ? advanced.getTransferAmount() / 1000 :
                            advanced.getTransferAmount(),
                    voiding.getFluidFilterContainer().getFilterWrapper().getFluidFilter() != null);
        }
    }

    /**
     * Displays text for {@link CoverItemFilter} related covers
     *
     * @param probeInfo  the info to add the text to
     * @param itemFilter the filter to get data from
     */
    private static void itemFilterInfo(@NotNull IProbeInfo probeInfo, @NotNull CoverItemFilter itemFilter) {
        filterModeText(probeInfo, itemFilter.getFilterMode());
        itemFilterText(probeInfo, itemFilter.getItemFilter().getItemFilter());
    }

    /**
     * Displays text for {@link CoverFluidFilter} related covers
     *
     * @param probeInfo   the info to add the text to
     * @param fluidFilter the filter to get data from
     */
    private static void fluidFilterInfo(@NotNull IProbeInfo probeInfo, @NotNull CoverFluidFilter fluidFilter) {
        filterModeText(probeInfo, fluidFilter.getFilterMode());
        fluidFilterText(probeInfo, fluidFilter.getFluidFilter().getFluidFilter());
    }

    /**
     * Displays text for {@link CoverEnderFluidLink} related covers
     *
     * @param probeInfo      the info to add the text to
     * @param enderFluidLink the ender fluid link cover to get data from
     */
    private static void enderFluidLinkInfo(@NotNull IProbeInfo probeInfo, @NotNull CoverEnderFluidLink enderFluidLink) {
        transferRateText(probeInfo, enderFluidLink.getPumpMode(), " {*cover.bucket.mode.milli_bucket*}",
                enderFluidLink.isIOEnabled() ? CoverEnderFluidLink.TRANSFER_RATE : 0);
        fluidFilterText(probeInfo, enderFluidLink.getFluidFilterContainer().getFilterWrapper().getFluidFilter());

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
    private static void transferRateText(@NotNull IProbeInfo probeInfo, @NotNull IIOMode mode, @NotNull String rateUnit,
                                         int rate) {
        String modeText = mode.isImport() ? "{*gregtech.top.mode.import*} " : "{*gregtech.top.mode.export*} ";
        probeInfo.text(TextStyleClass.OK + modeText + TextStyleClass.LABEL + TextFormattingUtil.formatNumbers(rate) +
                rateUnit);
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
    private static void transferModeText(@NotNull IProbeInfo probeInfo, @NotNull TransferMode mode,
                                         @NotNull String rateUnit, int rate, boolean hasFilter) {
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
    private static void voidingText(@NotNull IProbeInfo probeInfo, @NotNull VoidingMode mode, @NotNull String unit,
                                    int amount, boolean hasFilter) {
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
    private static void filterModeText(@NotNull IProbeInfo probeInfo, @NotNull IFilterMode mode) {
        probeInfo.text(TextStyleClass.WARNING + IProbeInfo.STARTLOC + mode.getName() + IProbeInfo.ENDLOC);
    }

    /**
     * Displays text for {@link ItemFilter} covers
     *
     * @param probeInfo the info to add the text to
     * @param filter    the filter to display info from
     */
    private static void itemFilterText(@NotNull IProbeInfo probeInfo, @Nullable ItemFilter filter) {
        String label = TextStyleClass.INFO + "{*gregtech.top.filter.label*} ";
        if (filter instanceof OreDictionaryItemFilter) {
            String expression = ((OreDictionaryItemFilter) filter).getExpression();
            if (!expression.isEmpty()) probeInfo.text(label + expression);
        } else if (filter instanceof SmartItemFilter) {
            probeInfo.text(label + IProbeInfo.STARTLOC + ((SmartItemFilter) filter).getFilteringMode().getName() +
                    IProbeInfo.ENDLOC);
        }
    }

    /**
     * Displays text for {@link FluidFilter} covers
     *
     * @param probeInfo the info to add the text to
     * @param filter    the filter to display info from
     */
    private static void fluidFilterText(@NotNull IProbeInfo probeInfo, @Nullable FluidFilter filter) {
        // TODO If more unique fluid filtration is added, providers for it go here
    }
}
