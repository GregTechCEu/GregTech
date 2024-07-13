package gregtech.common.pipelike.net.energy;

import gregtech.api.graphnet.logic.AbstractLongLogicData;
import gregtech.api.graphnet.logic.INetLogicEntry;

import org.jetbrains.annotations.NotNull;

public final class LossAbsoluteLogic extends AbstractLongLogicData<LossAbsoluteLogic> {

    public static final LossAbsoluteLogic INSTANCE = new LossAbsoluteLogic().setValue(0);

    @Override
    public @NotNull String getName() {
        return "LossAbsolute";
    }

    @Override
    public LossAbsoluteLogic getNew() {
        return new LossAbsoluteLogic();
    }

    @Override
    public LossAbsoluteLogic union(INetLogicEntry<?, ?> other) {
        if (other instanceof LossAbsoluteLogic l) {
            return this.getWith(this.getValue() + l.getValue());
        } else return this;
    }
}
