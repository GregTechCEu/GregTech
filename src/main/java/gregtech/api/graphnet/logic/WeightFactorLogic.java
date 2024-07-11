package gregtech.api.graphnet.logic;

import org.jetbrains.annotations.NotNull;

public final class WeightFactorLogic extends AbstractDoubleLogicData<WeightFactorLogic> {

    public static final WeightFactorLogic INSTANCE = new WeightFactorLogic().setValue(0.1d);

    @Override
    public @NotNull String getName() {
        return "WeightFactor";
    }

    @Override
    public WeightFactorLogic union(INetLogicEntry<?, ?> other) {
        if (other instanceof WeightFactorLogic l) {
            return new WeightFactorLogic().setValue(this.getValue() + l.getValue());
        } else return new WeightFactorLogic().setValue(this.getValue());

    }
}
