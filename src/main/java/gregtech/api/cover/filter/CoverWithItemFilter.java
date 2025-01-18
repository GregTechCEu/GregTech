package gregtech.api.cover.filter;

import gregtech.api.cover.Cover;
import gregtech.common.covers.ItemFilterMode;
import gregtech.common.covers.ManualImportExportMode;
import gregtech.common.covers.filter.ItemFilterContainer;

import org.jetbrains.annotations.Nullable;

public interface CoverWithItemFilter extends Cover {

    @Nullable
    ItemFilterContainer getItemFilter();

    ItemFilterMode getFilterMode();

    ManualImportExportMode getManualMode();
}
