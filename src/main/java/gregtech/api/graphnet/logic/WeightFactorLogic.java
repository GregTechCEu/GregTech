package gregtech.api.graphnet.logic;

import gregtech.api.GTValues;

import org.jetbrains.annotations.NotNull;

public final class WeightFactorLogic extends AbstractDoubleLogicData<WeightFactorLogic> {

    public static final DoubleLogicType<WeightFactorLogic> TYPE = new DoubleLogicType<>(GTValues.MODID, "WeightFactor",
            WeightFactorLogic::new, new WeightFactorLogic().setValue(0.1));

    @Override
    public @NotNull DoubleLogicType<WeightFactorLogic> getType() {
        return TYPE;
    }

    @Override
    public WeightFactorLogic union(NetLogicEntry<?, ?> other) {
        if (other instanceof WeightFactorLogic l) {
            return TYPE.getWith(this.getValue() + l.getValue());
        } else return this;
    }
}
