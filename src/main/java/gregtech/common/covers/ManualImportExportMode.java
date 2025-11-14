package gregtech.common.covers;

import gregtech.api.util.ITranslatable;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.ITooltip;
import org.jetbrains.annotations.NotNull;

public enum ManualImportExportMode implements ITranslatable {

    DISABLED("cover.universal.manual_import_export.mode.disabled"),
    FILTERED("cover.universal.manual_import_export.mode.filtered"),
    UNFILTERED("cover.universal.manual_import_export.mode.unfiltered");

    public static final ManualImportExportMode[] VALUES = values();
    private final String localeName;

    ManualImportExportMode(String localeName) {
        this.localeName = localeName;
    }

    @NotNull
    @Override
    public String getName() {
        return localeName;
    }

    @Override
    public void handleTooltip(@NotNull ITooltip<?> tooltip) {
        tooltip.addTooltipLine(IKey.lang(getName()));
        tooltip.addTooltipLine(IKey.lang(getName() + ".description"));
    }
}
