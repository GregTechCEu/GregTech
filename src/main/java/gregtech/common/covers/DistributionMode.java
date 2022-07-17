package gregtech.common.covers;

import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum DistributionMode implements IStringSerializable {
    ROUND_ROBIN_GLOBAL("cover.conveyor.distribution.round_robin_enhanced"),
    ROUND_ROBIN_PRIO("cover.conveyor.distribution.round_robin"),
    INSERT_FIRST("cover.conveyor.distribution.first_insert");

    public final String localeName;
    public final String localeTooltip;

    DistributionMode(String localeName) {
        this.localeName = localeName;
        this.localeTooltip = localeName + ".description";
    }

    @Nonnull
    @Override
    public String getName() {
        return localeName;
    }
}
