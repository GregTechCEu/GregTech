package gregtech.api.graphnet.logic;

import org.jetbrains.annotations.NotNull;

public final class ThroughputLogic extends AbstractIntLogicData<ThroughputLogic> {

    public static final ThroughputLogic INSTANCE = new ThroughputLogic().setValue(0);

    @Override
    public @NotNull String getName() {
        return "Throughput";
    }

    @Override
    public ThroughputLogic union(INetLogicEntry<?, ?> other) {
        if (other instanceof ThroughputLogic l) {
            return new ThroughputLogic().setValue(Math.min(this.getValue(), l.getValue()));
        } else return new ThroughputLogic().setValue(0);
    }
}
