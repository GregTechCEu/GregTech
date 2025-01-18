package gregtech.api.graphnet.logic;

import gregtech.api.GTValues;

import org.jetbrains.annotations.NotNull;

public final class ThroughputLogic extends AbstractLongLogicData<ThroughputLogic> {

    public static final LongLogicType<ThroughputLogic> TYPE = new LongLogicType<>(GTValues.MODID, "Throughput",
            ThroughputLogic::new, new ThroughputLogic());

    @Override
    public @NotNull LongLogicType<ThroughputLogic> getType() {
        return TYPE;
    }

    @Override
    public ThroughputLogic union(NetLogicEntry<?, ?> other) {
        if (other instanceof ThroughputLogic l) {
            return this.getValue() < l.getValue() ? this : l;
        } else return this;
    }
}
