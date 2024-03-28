package gregtech.common.covers;

import net.minecraft.util.IStringSerializable;

public enum ItemFilterMode implements IStringSerializable {

    FILTER_INSERT("cover.filter.mode.filter_insert"),
    FILTER_EXTRACT("cover.filter.mode.filter_extract"),
    FILTER_BOTH("cover.filter.mode.filter_both");

    public static final ItemFilterMode[] VALUES = values();
    public final String localeName;

    ItemFilterMode(String localeName) {
        this.localeName = localeName;
    }

    @Override
    public String getName() {
        return this.localeName;
    }
}
