package gregtech.common.pipelike.net.energy;

import gregtech.api.GTValues;
import gregtech.api.graphnet.logic.AbstractIntLogicData;

import org.jetbrains.annotations.NotNull;

public final class SuperconductorLogic extends AbstractIntLogicData<SuperconductorLogic> {

    public static final IntLogicType<SuperconductorLogic> TYPE = new IntLogicType<>(GTValues.MODID, "Superconductor",
            SuperconductorLogic::new, new SuperconductorLogic());

    @Override
    public @NotNull IntLogicType<SuperconductorLogic> getType() {
        return TYPE;
    }

    public boolean canSuperconduct(int temp) {
        return this.getValue() > temp;
    }
}
