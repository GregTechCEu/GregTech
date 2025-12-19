package gregtech.common.covers;

import gregtech.api.util.ITranslatable;

import org.jetbrains.annotations.NotNull;

public enum DistributionMode implements ITranslatable {

    ROUND_ROBIN_GLOBAL("cover.conveyor.distribution.round_robin_enhanced"),
    ROUND_ROBIN_PRIO("cover.conveyor.distribution.round_robin"),
    INSERT_FIRST("cover.conveyor.distribution.first_insert");

    public static final DistributionMode[] VALUES = values();
    private final String localeName;

    DistributionMode(String localeName) {
        this.localeName = localeName;
    }

    @NotNull
    @Override
    public String getName() {
        return localeName;
    }

    public boolean isRoundRobinGlobal() {
        return this == ROUND_ROBIN_GLOBAL;
    }

    public boolean isRoundRobinPriority() {
        return this == ROUND_ROBIN_PRIO;
    }

    public boolean isInsertFirst() {
        return this == INSERT_FIRST;
    }
}
