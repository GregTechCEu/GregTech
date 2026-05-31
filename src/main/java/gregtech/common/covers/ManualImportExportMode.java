package gregtech.common.covers;

import gregtech.api.util.ITranslatable;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.ITooltip;
import org.jetbrains.annotations.NotNull;

public enum ManualImportExportMode implements ITranslatable {

    DISABLED("cover.%s.manual_import_export.mode.disabled"),
    FILTERED("cover.%s.manual_import_export.mode.filtered"),
    UNFILTERED("cover.%s.manual_import_export.mode.unfiltered");

    public static final ManualImportExportMode[] VALUES = values();
    private final String localeName;

    ManualImportExportMode(String localeName) {
        this.localeName = localeName;
    }

    @NotNull
    @Override
    public String getName() {
        return getName("universal");
    }

    @Override
    public @NotNull String getName(@NotNull String key) {
        return String.format(localeName, key);
    }

    @Override
    public void handleTooltip(@NotNull ITooltip<?> tooltip, @NotNull String key) {
        tooltip.addTooltipLine(getKey());
        tooltip.addTooltipLine(IKey.lang(getName(key) + ".description"));
    }

    public boolean isDisabled() {
        return this == DISABLED;
    }

    public boolean isFiltered() {
        return this == FILTERED;
    }

    public boolean isUnfiltered() {
        return this == UNFILTERED;
    }
}
