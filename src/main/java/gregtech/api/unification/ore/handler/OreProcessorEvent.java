package gregtech.api.unification.ore.handler;

import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nonnull;

public class OreProcessorEvent extends Event {

    private final IOreProcessorHandler.Phase phase;

    public OreProcessorEvent(@Nonnull IOreProcessorHandler.Phase phase) {
        this.phase = phase;
    }

    @Nonnull
    public IOreProcessorHandler.Phase getRegistrationPhase() {
        return phase;
    }
}
