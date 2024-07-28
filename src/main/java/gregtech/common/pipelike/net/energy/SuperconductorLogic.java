package gregtech.common.pipelike.net.energy;

import gregtech.api.graphnet.logic.AbstractIntLogicData;

import org.jetbrains.annotations.NotNull;

public class SuperconductorLogic extends AbstractIntLogicData<SuperconductorLogic> {

    public static final SuperconductorLogic INSTANCE = new SuperconductorLogic().setValue(0);

    public boolean canSuperconduct(int temp) {
        return this.getValue() > temp;
    }

    @Override
    public @NotNull String getName() {
        return "Superconductor";
    }

    @Override
    public SuperconductorLogic getNew() {
        return new SuperconductorLogic();
    }
}
