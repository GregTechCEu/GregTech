package gregtech.common.covers;

import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

public enum DistributionMode implements IStringSerializable {

    EQUALIZED("cover.generic.distribution.equalized"),
    ROUND_ROBIN("cover.generic.distribution.round_robin"),
    FLOOD("cover.generic.distribution.flood");

    public static final DistributionMode[] VALUES = values();
    public final String localeName;

    DistributionMode(String localeName) {
        this.localeName = localeName;
    }

    @NotNull
    @Override
    public String getName() {
        return localeName;
    }
}
