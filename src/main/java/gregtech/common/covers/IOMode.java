package gregtech.common.covers;

import gregtech.api.util.ITranslatable;

import org.jetbrains.annotations.NotNull;

public enum IOMode implements ITranslatable {

    IMPORT("cover.%s.mode.import"),
    EXPORT("cover.%s.mode.export");

    public static final IOMode[] VALUES = values();
    public final String localeName;

    IOMode(String localeName) {
        this.localeName = localeName;
    }

    @Override
    public @NotNull String getName() {
        return getName("universal");
    }

    @Override
    public @NotNull String getName(@NotNull String key) {
        return String.format(localeName, key);
    }

    public boolean isImport() {
        return this == IMPORT;
    }

    public boolean isExport() {
        return this == EXPORT;
    }
}
