package gregtech.common.covers;

import gregtech.api.util.ITranslatable;

import org.jetbrains.annotations.NotNull;

public enum ItemFilterMode implements ITranslatable {

    FILTER_INSERT("cover.filter.mode.filter_insert"),
    FILTER_EXTRACT("cover.filter.mode.filter_extract"),
    FILTER_BOTH("cover.filter.mode.filter_both");

    public static final ItemFilterMode[] VALUES = values();
    private final String localeName;

    ItemFilterMode(String localeName) {
        this.localeName = localeName;
    }

    @NotNull
    @Override
    public String getName() {
        return this.localeName;
    }

    public boolean isFilterInsert() {
        return this == FILTER_INSERT;
    }

    public boolean isFilterExtract() {
        return this == FILTER_EXTRACT;
    }

    public boolean isFilterBoth() {
        return this == FILTER_BOTH;
    }
}
