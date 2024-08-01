package gregtech.common.pipelike.net.energy;

import gregtech.api.graphnet.logic.AbstractLongLogicData;
import gregtech.api.graphnet.logic.INetLogicEntry;

import org.jetbrains.annotations.NotNull;

public final class VoltageLossLogic extends AbstractLongLogicData<VoltageLossLogic> {

    public static final VoltageLossLogic INSTANCE = new VoltageLossLogic().setValue(0);

    @Override
    public @NotNull String getName() {
        return "LossAbsolute";
    }

    @Override
    public VoltageLossLogic getNew() {
        return new VoltageLossLogic();
    }

    @Override
    public VoltageLossLogic union(INetLogicEntry<?, ?> other) {
        if (other instanceof VoltageLossLogic l) {
            return this.getWith(this.getValue() + l.getValue());
        } else return this;
    }
}
