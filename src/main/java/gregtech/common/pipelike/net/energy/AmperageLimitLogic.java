package gregtech.common.pipelike.net.energy;

import gregtech.api.GTValues;
import gregtech.api.graphnet.logic.AbstractLongLogicData;
import gregtech.api.graphnet.logic.NetLogicEntry;

import org.jetbrains.annotations.NotNull;

public final class AmperageLimitLogic extends AbstractLongLogicData<AmperageLimitLogic> {

    public static final LongLogicType<AmperageLimitLogic> TYPE = new LongLogicType<>(GTValues.MODID, "AmperageLimit",
            AmperageLimitLogic::new, new AmperageLimitLogic());

    @Override
    public @NotNull LongLogicType<AmperageLimitLogic> getType() {
        return TYPE;
    }

    @Override
    public AmperageLimitLogic union(NetLogicEntry<?, ?> other) {
        if (other instanceof AmperageLimitLogic l) {
            return this.getValue() < l.getValue() ? this : l;
        } else return this;
    }
}
