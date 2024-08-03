package gregtech.common.pipelike.net.energy;

import gregtech.api.graphnet.logic.AbstractLongLogicData;
import gregtech.api.graphnet.logic.NetLogicEntry;

import org.jetbrains.annotations.NotNull;

public final class VoltageLossLogic extends AbstractLongLogicData<VoltageLossLogic> {

    public static final VoltageLossLogic INSTANCE = new VoltageLossLogic().setValue(0);

    private VoltageLossLogic() {
        super("VoltageLoss");
    }

    @Override
    public @NotNull VoltageLossLogic getNew() {
        return new VoltageLossLogic();
    }

    @Override
    public VoltageLossLogic union(NetLogicEntry<?, ?> other) {
        if (other instanceof VoltageLossLogic l) {
            return this.getWith(this.getValue() + l.getValue());
        } else return this;
    }
}
