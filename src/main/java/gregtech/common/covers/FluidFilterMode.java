package gregtech.common.covers;

import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

public enum FluidFilterMode implements IStringSerializable {

    FILTER_FILL("cover.fluid_filter.mode.filter_fill"),
    FILTER_DRAIN("cover.fluid_filter.mode.filter_drain"),
    FILTER_BOTH("cover.fluid_filter.mode.filter_both");

    public static final FluidFilterMode[] VALUES = values();
    public final String localeName;

    FluidFilterMode(String localeName) {
        this.localeName = localeName;
    }

    @Override
    public @NotNull String getName() {
        return this.localeName;
    }
}
