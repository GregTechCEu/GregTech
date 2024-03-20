package gregtech.common.covers;

import net.minecraft.util.IStringSerializable;

public enum FluidFilterMode implements IStringSerializable {

    FILTER_FILL("cover.fluid_filter.mode.filter_fill"),
    FILTER_DRAIN("cover.fluid_filter.mode.filter_drain"),
    FILTER_BOTH("cover.fluid_filter.mode.filter_both");

    public final String localeName;

    FluidFilterMode(String localeName) {
        this.localeName = localeName;
    }

    @Override
    public String getName() {
        return this.localeName;
    }
}
