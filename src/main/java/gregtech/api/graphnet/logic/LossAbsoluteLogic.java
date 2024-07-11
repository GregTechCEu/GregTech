package gregtech.api.graphnet.logic;

import org.jetbrains.annotations.NotNull;

public final class LossAbsoluteLogic extends AbstractIntLogicData<LossAbsoluteLogic> {

    public static final LossAbsoluteLogic INSTANCE = new LossAbsoluteLogic().setValue(0);

    @Override
    public @NotNull String getName() {
        return "LossAbsolute";
    }

    @Override
    public LossAbsoluteLogic union(INetLogicEntry<?, ?> other) {
        if (other instanceof LossAbsoluteLogic l) {
            return new LossAbsoluteLogic().setValue((this.getValue() + l.getValue()) / 2);
        } else return new LossAbsoluteLogic().setValue(this.getValue() / 2);
    }
}
