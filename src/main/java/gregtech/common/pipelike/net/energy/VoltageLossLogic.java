package gregtech.common.pipelike.net.energy;

import gregtech.api.GTValues;
import gregtech.api.graphnet.logic.AbstractDoubleLogicData;
import gregtech.api.graphnet.logic.NetLogicEntry;

import org.jetbrains.annotations.NotNull;

public final class VoltageLossLogic extends AbstractDoubleLogicData<VoltageLossLogic> {

    public static final DoubleLogicType<VoltageLossLogic> TYPE = new DoubleLogicType<>(GTValues.MODID, "VoltageLoss",
            VoltageLossLogic::new, new VoltageLossLogic());

    @Override
    public @NotNull DoubleLogicType<VoltageLossLogic> getType() {
        return TYPE;
    }

    @Override
    public VoltageLossLogic union(NetLogicEntry<?, ?> other) {
        if (other instanceof VoltageLossLogic l) {
            return TYPE.getWith(this.getValue() + l.getValue());
        } else return this;
    }
}
