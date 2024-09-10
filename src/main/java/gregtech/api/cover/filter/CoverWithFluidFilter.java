package gregtech.api.cover.filter;

import gregtech.api.cover.Cover;
import gregtech.common.covers.FluidFilterMode;
import gregtech.common.covers.ManualImportExportMode;
import gregtech.common.covers.filter.FluidFilterContainer;

import org.jetbrains.annotations.Nullable;

public interface CoverWithFluidFilter extends Cover {

    @Nullable
    FluidFilterContainer getFluidFilter();

    FluidFilterMode getFilterMode();

    ManualImportExportMode getManualMode();
}
