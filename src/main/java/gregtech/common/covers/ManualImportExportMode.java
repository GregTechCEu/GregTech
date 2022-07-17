package gregtech.common.covers;

import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum ManualImportExportMode implements IStringSerializable {

    DISABLED("cover.universal.manual_import_export.mode.disabled"),
    FILTERED("cover.universal.manual_import_export.mode.filtered"),
    UNFILTERED("cover.universal.manual_import_export.mode.unfiltered");

    public final String localeName;
    public final String localeTooltip;

    ManualImportExportMode(String localeName) {
        this.localeName = localeName;
        this.localeTooltip = localeName + ".description";
    }

    @Nonnull
    @Override
    public String getName() {
        return localeName;
    }
}
