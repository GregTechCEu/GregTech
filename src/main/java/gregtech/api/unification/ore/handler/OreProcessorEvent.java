package gregtech.api.unification.ore.handler;

import net.minecraftforge.fml.common.eventhandler.Event;

import org.jetbrains.annotations.NotNull;

/**
 * Fired when the {@link #registrationPhase()} for OreProcessors changes.
 * <p>
 * Be sure to check the current phase before use.
 */
public class OreProcessorEvent extends Event {

    private final OreProcessorManager.Phase phase;

    public OreProcessorEvent(@NotNull OreProcessorManager.Phase phase) {
        this.phase = phase;
    }

    public @NotNull OreProcessorManager.Phase registrationPhase() {
        return phase;
    }
}
