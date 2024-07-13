package gregtech.common.pipelike.net.energy;

import gregtech.api.graphnet.logic.AbstractLongLogicData;
import gregtech.api.graphnet.logic.INetLogicEntry;

import org.jetbrains.annotations.NotNull;

public final class VoltageLimitLogic extends AbstractLongLogicData<VoltageLimitLogic> {

    public static final VoltageLimitLogic INSTANCE = new VoltageLimitLogic().setValue(0);

    @Override
    public @NotNull String getName() {
        return "VoltageLimit";
    }

    @Override
    public VoltageLimitLogic getNew() {
        return new VoltageLimitLogic().setValue(INSTANCE.getValue());
    }

    @Override
    public VoltageLimitLogic union(INetLogicEntry<?, ?> other) {
        if (other instanceof VoltageLimitLogic l) {
            return this.getValue() < l.getValue() ? this : l;
        } else return this;
    }
}
