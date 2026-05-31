package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.Cover;
import gregtech.api.cover.CoverHolder;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.covers.*;
import gregtech.common.covers.ender.CoverEnderFluidLink;
import gregtech.common.covers.filter.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IStringSerializable;
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
        String rateUnit = lang("cover.conveyor.transfer_rate");

        if (conveyor instanceof CoverItemVoiding coverItemVoiding) {
            itemVoidingInfo(probeInfo, coverItemVoiding);
        } else if (!(conveyor instanceof CoverRoboticArm arm) || arm.getTransferMode().isTransferAny()) {
            // only display the regular rate if the cover does not have a specialized rate
            transferRateText(probeInfo, conveyor.getIOMode(), " " + rateUnit, conveyor.getTransferRate());
        }

        ItemFilterContainer filter = conveyor.getItemFilterContainer();
        if (conveyor instanceof CoverRoboticArm roboticArm) {
            TransferMode transferMode = roboticArm.getTransferMode();
            if (!transferMode.isTransferAny()) {
                rateUnit = lang("cover.robotic_arm.exact");
            }

            transferModeText(probeInfo, transferMode, "robotic_arm", rateUnit, filter.getTransferSize(),
                    filter.hasFilter());
        }
        itemFilterText(probeInfo, filter.getFilter());
    }

    /**
     * Displays info for {@link CoverItemVoiding} related covers
     *
     * @param probeInfo the info to add the text to
     * @param voiding   the voiding cover to get data from
     */
    private static void itemVoidingInfo(@NotNull IProbeInfo probeInfo, @NotNull CoverItemVoiding voiding) {
        String unit = lang("gregtech.top.unit.items");

        ItemFilterContainer container = voiding.getItemFilterContainer();
        if (voiding instanceof CoverItemVoidingAdvanced advanced) {
            VoidingMode mode = advanced.getVoidingMode();
            voidingText(probeInfo, mode, unit, container.getTransferSize(),
                    container.hasFilter() && !container.isBlacklistFilter());
        }
    }

    /**
     * Displays text for {@link CoverPump} related covers
     *
     * @param probeInfo the info to add the text to
     * @param pump      the pump to get data from
     */
    private static void pumpInfo(@NotNull IProbeInfo probeInfo, @NotNull CoverPump pump) {
        String rateUnit = lang(pump.getBucketMode() == CoverPump.BucketMode.BUCKET ?
                "cover.bucket.mode.bucket_rate" :
                "cover.bucket.mode.milli_bucket_rate");

        if (pump instanceof CoverFluidVoiding coverFluidVoiding) {
            fluidVoidingInfo(probeInfo, coverFluidVoiding);
        } else if (!(pump instanceof CoverFluidRegulator regulator) ||
                regulator.getTransferMode() == TransferMode.TRANSFER_ANY) {
                    // do not display the regular rate if the cover has a specialized rate
                    transferRateText(probeInfo, pump.getIoMode(), " " + rateUnit,
                            pump.getBucketMode() == CoverPump.BucketMode.BUCKET ? pump.getTransferRate() / 1000 :
                                    pump.getTransferRate());
                }

        FluidFilterContainer filter = pump.getFluidFilterContainer();
        if (pump instanceof CoverFluidRegulator regulator) {
            if (regulator.getTransferMode() != TransferMode.TRANSFER_ANY)
                rateUnit = lang(regulator.getBucketMode() == CoverPump.BucketMode.BUCKET ?
                        "gregtech.top.unit.fluid_buckets" :
                        "gregtech.top.unit.fluid_milibuckets");

            transferModeText(probeInfo, regulator.getTransferMode(), "fluid_regulator", rateUnit,
                    filter.getTransferSize(), filter.hasFilter() && !filter.isBlacklistFilter());
        }
        fluidFilterText(probeInfo, filter.getFilter());
    }

    /**
     * Displays info for {@link CoverFluidVoiding} related covers
     *
     * @param probeInfo the info to add the text to
     * @param voiding   the voiding cover to get data from
     */
    private static void fluidVoidingInfo(@NotNull IProbeInfo probeInfo, @NotNull CoverFluidVoiding voiding) {
        String unit = lang(voiding.getBucketMode().isFullBuckets() ?
                "gregtech.top.unit.fluid_buckets" :
                "gregtech.top.unit.fluid_milibuckets");

        if (voiding instanceof CoverFluidVoidingAdvanced advanced) {
            FluidFilterContainer container = voiding.getFluidFilterContainer();
            VoidingMode mode = advanced.getVoidingMode();
            // do not display amount in overflow when a filter is present
            voidingText(probeInfo, mode, unit, voiding.getBucketMode().fromMilliBuckets(advanced.getTransferAmount()),
                    container.hasFilter() && !container.isBlacklistFilter());
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
        itemFilterText(probeInfo, itemFilter.getFilter());
    }

    /**
     * Displays text for {@link CoverFluidFilter} related covers
     *
     * @param probeInfo   the info to add the text to
     * @param fluidFilter the filter to get data from
     */
    private static void fluidFilterInfo(@NotNull IProbeInfo probeInfo, @NotNull CoverFluidFilter fluidFilter) {
        filterModeText(probeInfo, fluidFilter.getFilterMode());
        fluidFilterText(probeInfo, fluidFilter.getFilter());
    }

    /**
     * Displays text for {@link CoverEnderFluidLink} related covers
     *
     * @param probeInfo      the info to add the text to
     * @param enderFluidLink the ender fluid link cover to get data from
     */
    private static void enderFluidLinkInfo(@NotNull IProbeInfo probeInfo, @NotNull CoverEnderFluidLink enderFluidLink) {
        transferRateText(probeInfo, enderFluidLink.getPumpMode(), " " + lang("cover.ender_fluid_link.transfer_unit"),
                enderFluidLink.isIoEnabled() ? CoverEnderFluidLink.TRANSFER_RATE : 0);
        fluidFilterText(probeInfo, enderFluidLink.getFluidFilterContainer().getFilter());

        if (!enderFluidLink.getColorStr().isEmpty()) {
            probeInfo.text(
                    TextStyleClass.INFO + lang("gregtech.top.link_cover.color") + " " + enderFluidLink.getColorStr());
        }
    }

    /**
     * Displays text for {@link IOMode} covers
     *
     * @param probeInfo the info to add the text to
     * @param mode      the transfer mode of the cover
     * @param rateUnit  the unit of what is transferred
     * @param rate      the transfer rate of the mode
     */
    private static void transferRateText(@NotNull IProbeInfo probeInfo, @NotNull IOMode mode, @NotNull String rateUnit,
                                         int rate) {
        String modeText = mode.isImport() ? lang("gregtech.top.mode.import") : lang("gregtech.top.mode.export");
        modeText += " ";
        probeInfo.text(TextStyleClass.OK + modeText + TextStyleClass.LABEL + TextFormattingUtil.formatNumbers(rate) +
                rateUnit);
    }

    /**
     * Displays text for {@link TransferMode} covers
     *
     * @param probeInfo the info to add the text to
     * @param mode      the transfer mode of the cover
     * @param rate      the transfer rate of the mode
     * @param hasFilter whether the cover has a filter installed
     */
    private static void transferModeText(@NotNull IProbeInfo probeInfo, @NotNull TransferMode mode,
                                         @NotNull String coverName, @NotNull String rateUnit, int rate,
                                         boolean hasFilter) {
        String text = TextStyleClass.OK + lang(mode.getName(coverName));
        if (!hasFilter && !mode.isTransferAny()) {
            text += TextStyleClass.LABEL + " " + TextFormattingUtil.formatNumbers(rate) + " " + rateUnit;
        }
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
        String text = TextFormatting.RED + lang(mode.getName());
        if (mode != VoidingMode.VOID_ANY && !hasFilter) {
            text += " " + TextFormattingUtil.formatNumbers(amount) + " " + unit;
        }
        probeInfo.text(text);
    }

    /**
     * Displays text for {@link net.minecraft.util.IStringSerializable} covers
     *
     * @param probeInfo the info to add the text to
     * @param mode      the filter mode of the cover
     */
    private static void filterModeText(@NotNull IProbeInfo probeInfo, @NotNull IStringSerializable mode) {
        probeInfo.text(TextStyleClass.WARNING + lang(mode.getName()));
    }

    /**
     * Displays text for {@link BaseFilter} item covers
     *
     * @param probeInfo the info to add the text to
     * @param filter    the filter to display info from
     */
    private static void itemFilterText(@NotNull IProbeInfo probeInfo, @Nullable BaseFilter filter) {
        String label = TextStyleClass.INFO + lang("gregtech.top.filter.label");
        if (filter instanceof OreDictionaryItemFilter oreDictionaryItemFilter) {
            String expression = oreDictionaryItemFilter.getExpression();
            if (!expression.isEmpty()) probeInfo.text(label + expression);
        } else if (filter instanceof SmartItemFilter smartItemFilter) {
            probeInfo.text(label + lang(smartItemFilter.getFilteringMode().getName()));
        }
    }

    /**
     * Displays text for {@link BaseFilter} fluid covers
     *
     * @param probeInfo the info to add the text to
     * @param filter    the filter to display info from
     */
    private static void fluidFilterText(@NotNull IProbeInfo probeInfo, @Nullable BaseFilter filter) {
        // TODO If more unique fluid filtration is added, providers for it go here
    }

    private static String lang(String lang) {
        return IProbeInfo.STARTLOC + lang + IProbeInfo.ENDLOC;
    }
}
