package gregtech.common.covers;

import gregtech.api.util.ITranslatable;

import org.jetbrains.annotations.NotNull;

public enum FluidFilterMode implements ITranslatable {

    FILTER_FILL("cover.fluid_filter.mode.filter_fill"),
    FILTER_DRAIN("cover.fluid_filter.mode.filter_drain"),
    FILTER_BOTH("cover.fluid_filter.mode.filter_both");

    public static final FluidFilterMode[] VALUES = values();
    private final String localeName;

    FluidFilterMode(String localeName) {
        this.localeName = localeName;
    }

    @NotNull
    @Override
    public String getName() {
        return this.localeName;
    }
}
