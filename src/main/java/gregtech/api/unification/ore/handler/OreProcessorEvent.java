package gregtech.api.unification.ore.handler;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired when the {@link OreProcessorManager.Phase} for OreProcessors changes.
 */
public abstract class OreProcessorEvent extends Event {

    protected OreProcessorEvent() {}

    /**
     * Event during which {@link OreProcessor}s can be registered with an {@link OreProcessorManager}
     */
    public static class Registration extends OreProcessorEvent {}

    /**
     * Event during which {@link OreProcessor}s can be removed with an {@link OreProcessorManager}
     */
    public static class Removal extends OreProcessorEvent {}
}
