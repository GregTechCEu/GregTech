package gregtech.common.pipelike.net.energy;

import gregtech.api.GTValues;
import gregtech.api.graphnet.logic.AbstractByteLogicData;

import org.jetbrains.annotations.NotNull;

public final class SuperconductorLogic extends AbstractByteLogicData<SuperconductorLogic> {

    public static final SuperconductorLogic INSTANCE = new SuperconductorLogic();

    public static final ByteLogicType<SuperconductorLogic> TYPE = new ByteLogicType<>(GTValues.MODID, "Superconductor",
            () -> INSTANCE, INSTANCE);

    @Override
    public @NotNull ByteLogicType<SuperconductorLogic> getType() {
        return TYPE;
    }
}
