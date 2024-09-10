package gregtech.common.pipelike.net.energy;

import gregtech.api.GTValues;
import gregtech.api.graphnet.logic.AbstractLongLogicData;
import gregtech.api.graphnet.logic.NetLogicEntry;

import org.jetbrains.annotations.NotNull;

public final class VoltageLossLogic extends AbstractLongLogicData<VoltageLossLogic> {

    public static final LongLogicType<VoltageLossLogic> TYPE = new LongLogicType<>(GTValues.MODID, "VoltageLoss",
            VoltageLossLogic::new, new VoltageLossLogic());

    @Override
    public @NotNull LongLogicType<VoltageLossLogic> getType() {
        return TYPE;
    }

    @Override
    public VoltageLossLogic union(NetLogicEntry<?, ?> other) {
        if (other instanceof VoltageLossLogic l) {
            return TYPE.getWith(this.getValue() + l.getValue());
        } else return this;
    }
}
