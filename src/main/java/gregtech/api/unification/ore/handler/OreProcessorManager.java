package gregtech.api.unification.ore.handler;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.function.TriConsumer;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface OreProcessorManager {

    /**
     * Register a processor for an OrePrefix.
     *
     * @param prefix  the OrePrefix to associate with the processor
     * @param name    the name of the processor
     * @param handler the handler to register
     * @throws IllegalArgumentException if a handler with the same name has already been registered for the OrePrefix
     */
    void registerProcessor(@NotNull OrePrefix prefix, @NotNull ResourceLocation name, @NotNull OreProcessor handler);

    /**
     * Register a processor for an OrePrefix.
     *
     * @param prefix      the OrePrefix to associate with the processor
     * @param name        the name of the processor
     * @param propertyKey the property key materials must have in order to be processed in the handler
     * @param handler     the handler to register
     * @throws IllegalArgumentException if a handler with the same name has already been registered for the OrePrefix
     */
    <T extends IMaterialProperty> void registerProcessor(@NotNull OrePrefix prefix, @NotNull ResourceLocation name,
                                                         @NotNull PropertyKey<T> propertyKey,
                                                         @NotNull TriConsumer<OrePrefix, Material, T> handler);

    /**
     * Remove an {@link OreProcessor} for an OrePrefix.
     *
     * @param prefix the OrePrefix associated with the processor
     * @param name   the name of the handler
     * @return if removal was successful
     */
    @SuppressWarnings("unused")
    boolean removeHandler(@NotNull OrePrefix prefix, @NotNull ResourceLocation name);

    /**
     * @param prefix the prefix to get handlers for
     * @return all the registered handler names for the OrePrefix
     */
    @SuppressWarnings("unused")
    @NotNull
    Collection<@NotNull ResourceLocation> getRegisteredHandlerNames(@NotNull OrePrefix prefix);

    /**
     * @return the current phase of the handler
     */
    @NotNull
    Phase getPhase();

    enum Phase {
        /** Phase when new handlers can be registered */
        REGISTRATION,
        /** Phase when handlers can be removed */
        REMOVAL,
        /** Phase when handlers are run */
        PROCESSING,
        /** Phase when handlers are run late for other mods like CraftTweaker */
        PROCESSING_POST
    }
}
